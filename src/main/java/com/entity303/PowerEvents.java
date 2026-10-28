package com.entity303;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Entity303Mod.MODID)
public class PowerEvents {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ModItems.EYES);
    }

    /** Passive powers for whoever killed Entity 303. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) return;
        EntityPlayer p = event.player;
        if (!Powers.has(p)) return;

        if (p.ticksExisted % 20 == 0) {
            p.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 100, 0, true, false));
            p.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 100, 0, true, false));
            p.addPotionEffect(new PotionEffect(MobEffects.SPEED, 100, 0, true, false));
        }
        // if the power item was lost, give it back
        if (p.ticksExisted % 100 == 0 && !Powers.hasItem(p)) {
            Powers.giveItem(p);
        }
    }

    /** Entity 303 never takes fall damage - neither do his heirs. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && Powers.has((EntityPlayer) event.getEntityLiving())) {
            event.setCanceled(true);
        }
    }
}
