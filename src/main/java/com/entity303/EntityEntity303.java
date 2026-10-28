package com.entity303;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;

public class EntityEntity303 extends EntityMob {

    private final BossInfoServer bossInfo = new BossInfoServer(
            new TextComponentString("Entity 303"), BossInfo.Color.RED, BossInfo.Overlay.PROGRESS);

    private int laserCooldown = 40;
    private int teleportCooldown = 60;

    public EntityEntity303(World world) {
        super(world);
        this.setSize(0.6F, 1.95F);
        this.isImmuneToFire = true;
        this.experienceValue = 200;
        this.enablePersistence(); // never despawns
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(7, new EntityAIWander(this, 0.8D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 24.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (this.world.isRemote) {
            // dark smoke around him
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    this.posX + (this.rand.nextDouble() - 0.5D) * this.width,
                    this.posY + this.rand.nextDouble() * this.height,
                    this.posZ + (this.rand.nextDouble() - 0.5D) * this.width,
                    0.0D, 0.0D, 0.0D);
            return;
        }

        // fake "joined the game" message
        if (this.ticksExisted == 1 && this.getServer() != null) {
            TextComponentTranslation msg = new TextComponentTranslation("multiplayer.player.joined", "Entity303");
            msg.getStyle().setColor(TextFormatting.YELLOW);
            this.getServer().getPlayerList().sendMessage(msg);
        }

        if (this.laserCooldown > 0) this.laserCooldown--;
        if (this.teleportCooldown > 0) this.teleportCooldown--;

        EntityLivingBase target = this.getAttackTarget();
        if (target == null || !target.isEntityAlive()) return;

        double distSq = this.getDistanceSq(target);

        // 1) Red eye lasers
        if (this.laserCooldown <= 0 && distSq < 32.0D * 32.0D && this.canEntityBeSeen(target)) {
            fireLaser(target);
            this.laserCooldown = 30 + this.rand.nextInt(40);
        }

        // 2) Teleport close to far-away targets
        if (this.teleportCooldown <= 0 && distSq > 12.0D * 12.0D) {
            teleportNear(target);
        }

        // 3) Lightning strike on target
        if (this.rand.nextInt(400) == 0) {
            this.world.addWeatherEffect(new EntityLightningBolt(this.world, target.posX, target.posY, target.posZ, false));
        }

        // 4) Fear aura: slowness + nausea (screen wobble) for nearby players
        if (this.ticksExisted % 100 == 0) {
            for (EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(20.0D))) {
                p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 0));
                if (this.rand.nextInt(4) == 0) {
                    p.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 120, 0));
                }
            }
        }
    }

    private void fireLaser(EntityLivingBase target) {
        Vec3d eye = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
        Vec3d end = new Vec3d(target.posX, target.posY + target.height * 0.6D, target.posZ);
        Vec3d dir = end.subtract(eye).normalize();
        Vec3d side = new Vec3d(-dir.z, 0.0D, dir.x).normalize();
        WorldServer ws = (WorldServer) this.world;

        // one beam from each eye
        for (int s = -1; s <= 1; s += 2) {
            Vec3d start = eye.add(side.scale(0.12D * s)).add(dir.scale(0.3D));
            Vec3d beam = end.subtract(start);
            Vec3d step = beam.normalize().scale(0.35D);
            int n = (int) (beam.lengthVector() / 0.35D);
            Vec3d p = start;
            for (int i = 0; i < n; i++) {
                // REDSTONE with count 0: offsets are the RGB color -> pure red
                ws.spawnParticle(EnumParticleTypes.REDSTONE, true, p.x, p.y, p.z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
                p = p.add(step);
            }
        }

        this.world.playSound(null, this.posX, this.posY, this.posZ,
                SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.5F, 0.5F);
        target.attackEntityFrom(DamageSource.causeMobDamage(this), 8.0F);
        target.setFire(5);
    }

    private void teleportNear(EntityLivingBase target) {
        for (int i = 0; i < 10; i++) {
            double angle = this.rand.nextDouble() * Math.PI * 2.0D;
            double dist = 4.0D + this.rand.nextDouble() * 3.0D;
            double x = target.posX + Math.cos(angle) * dist;
            double z = target.posZ + Math.sin(angle) * dist;
            if (this.attemptTeleport(x, target.posY, z)) {
                this.teleportCooldown = 80 + this.rand.nextInt(100);
                return;
            }
        }
        this.teleportCooldown = 40;
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        boolean hit = super.attackEntityAsMob(entity);
        if (hit && entity instanceof EntityLivingBase) {
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.WITHER, 100, 1));
        }
        return hit;
    }

    // ---- boss bar ----
    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
    }

    // ---- death / loot / sounds ----
    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!this.world.isRemote) {
            Entity killer = cause.getTrueSource();
            if (killer instanceof EntityPlayer) {
                Powers.grant((EntityPlayer) killer);
            }
        }
        if (!this.world.isRemote && this.getServer() != null) {
            TextComponentTranslation msg = new TextComponentTranslation("multiplayer.player.left", "Entity303");
            msg.getStyle().setColor(TextFormatting.YELLOW);
            this.getServer().getPlayerList().sendMessage(msg);
        }
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        this.dropItem(Items.NETHER_STAR, 1);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITHER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITHER_DEATH;
    }
}
