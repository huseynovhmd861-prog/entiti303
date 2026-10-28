package com.entity303;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

public class Powers {
    public static final String TAG = "entity303_powers";

    /** Stored in PlayerPersisted data, so it survives death/respawn. */
    public static boolean has(EntityPlayer player) {
        return player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean(TAG);
    }

    public static void grant(EntityPlayer player) {
        if (has(player)) return;
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persisted.setBoolean(TAG, true);
        data.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);

        player.world.addWeatherEffect(new EntityLightningBolt(player.world, player.posX, player.posY, player.posZ, true));
        player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Entity 303-un gucleri sene kecdi!"));
        player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Qirmizi lazer, teleportasiya, od toxunulmazligi, guc ve sur'et."));
        giveItem(player);
    }

    public static void giveItem(EntityPlayer player) {
        ItemStack stack = new ItemStack(ModItems.EYES);
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropItem(stack, false);
        }
    }

    public static boolean hasItem(EntityPlayer player) {
        for (ItemStack s : player.inventory.mainInventory) if (s.getItem() == ModItems.EYES) return true;
        for (ItemStack s : player.inventory.offHandInventory) if (s.getItem() == ModItems.EYES) return true;
        return false;
    }

    /** Draws a red particle beam between two points. */
    public static void beam(WorldServer ws, Vec3d start, Vec3d end) {
        Vec3d diff = end.subtract(start);
        int n = (int) (diff.lengthVector() / 0.35D);
        Vec3d step = diff.normalize().scale(0.35D);
        Vec3d p = start;
        for (int i = 0; i < n; i++) {
            ws.spawnParticle(EnumParticleTypes.REDSTONE, true, p.x, p.y, p.z, 0, 1.0D, 0.0D, 0.0D, 1.0D);
            p = p.add(step);
        }
    }
}
