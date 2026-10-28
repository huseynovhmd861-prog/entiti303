package com.entity303;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod(modid = Entity303Mod.MODID, name = "Entity 303", version = "1.0", acceptedMinecraftVersions = "[1.12.2]")
public class Entity303Mod {
    public static final String MODID = "entity303";

    @Mod.Instance(MODID)
    public static Entity303Mod instance;

    @SidedProxy(clientSide = "com.entity303.ClientProxy", serverSide = "com.entity303.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // name, id, tracking range, update frequency, velocity updates, egg colors (white / red)
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "entity_303"),
                EntityEntity303.class, "entity_303", 0, this, 80, 3, true, 0xFFFFFF, 0xFF0000);
        proxy.preInit();
    }
}
