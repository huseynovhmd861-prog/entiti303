package com.entity303;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntity303 extends RenderBiped<EntityEntity303> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Entity303Mod.MODID, "textures/entity/entity_303.png");

    public RenderEntity303(RenderManager manager) {
        // 64x64 texture layout
        super(manager, new ModelBiped(0.0F, 0.0F, 64, 64), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEntity303 entity) {
        return TEXTURE;
    }
}
