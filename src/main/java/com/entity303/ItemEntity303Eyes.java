package com.entity303;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEntity303Eyes extends Item {

    private static final double RANGE = 40.0D;

    public ItemEntity303Eyes() {
        this.setRegistryName(new ResourceLocation(Entity303Mod.MODID, "entity303_eyes"));
        this.setUnlocalizedName("entity303_eyes");
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, stack);

        if (!Powers.has(player)) {
            player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Bu gucu ancaq Entity 303-u oldurenler istifade ede biler."));
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        if (player.getCooldownTracker().hasCooldown(this)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        if (player.isSneaking()) {
            teleport(world, player);
            player.getCooldownTracker().setCooldown(this, 40);
        } else {
            laser(world, player);
            player.getCooldownTracker().setCooldown(this, 15);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private void laser(World world, EntityPlayer player) {
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec();
        Vec3d end = eye.add(look.scale(RANGE));

        RayTraceResult blockHit = world.rayTraceBlocks(eye, end, false, true, false);
        if (blockHit != null) end = blockHit.hitVec;

        // find closest living entity on the beam
        EntityLivingBase best = null;
        double bestDist = Double.MAX_VALUE;
        AxisAlignedBB area = player.getEntityBoundingBox().expand(look.x * RANGE, look.y * RANGE, look.z * RANGE).grow(1.0D);
        List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, area);
        for (EntityLivingBase e : list) {
            if (e == player) continue;
            RayTraceResult r = e.getEntityBoundingBox().grow(0.3D).calculateIntercept(eye, end);
            if (r != null) {
                double d = eye.squareDistanceTo(r.hitVec);
                if (d < bestDist) { bestDist = d; best = e; }
            }
        }
        if (best != null) {
            end = new Vec3d(best.posX, best.posY + best.height * 0.6D, best.posZ);
            best.attackEntityFrom(DamageSource.causePlayerDamage(player), 10.0F);
            best.setFire(6);
        }

        Vec3d side = new Vec3d(-look.z, 0.0D, look.x).normalize();
        WorldServer ws = (WorldServer) world;
        for (int s = -1; s <= 1; s += 2) {
            Vec3d start = eye.add(side.scale(0.15D * s)).add(look.scale(0.4D)).addVector(0.0D, -0.05D, 0.0D);
            Powers.beam(ws, start, end);
        }
        world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.2F, 0.5F);
    }

    private void teleport(World world, EntityPlayer player) {
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec();
        Vec3d end = eye.add(look.scale(RANGE));
        double x = end.x, y = end.y, z = end.z;

        RayTraceResult hit = world.rayTraceBlocks(eye, end, false, true, false);
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos().offset(hit.sideHit);
            x = pos.getX() + 0.5D;
            y = pos.getY();
            z = pos.getZ() + 0.5D;
        }

        WorldServer ws = (WorldServer) world;
        ws.spawnParticle(EnumParticleTypes.PORTAL, player.posX, player.posY + 1.0D, player.posZ, 40, 0.3D, 0.8D, 0.3D, 0.5D);
        ((EntityPlayerMP) player).connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
        ws.spawnParticle(EnumParticleTypes.PORTAL, x, y + 1.0D, z, 40, 0.3D, 0.8D, 0.3D, 0.5D);
        world.playSound(null, x, y, z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.RED + "Sag klik: qirmizi lazer");
        tooltip.add(TextFormatting.DARK_PURPLE + "Shift + sag klik: teleportasiya");
        tooltip.add(TextFormatting.GRAY + "Ancaq Entity 303-u oldurenler ucun");
    }
}
