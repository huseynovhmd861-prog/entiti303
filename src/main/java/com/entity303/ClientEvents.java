package com.entity303;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Entity303Mod.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void onModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(ModItems.EYES, 0,
                new ModelResourceLocation(ModItems.EYES.getRegistryName(), "inventory"));
    }
}
