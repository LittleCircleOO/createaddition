package com.mrh0.createaddition.compat.rei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.index.CABlocks;
import com.simibubi.create.compat.rei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

public class AnimatedRollingMill extends AnimatedKinetics {

    @Override
    public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 0);
        AllGuiTextures.JEI_SHADOW.render(matrixStack, -16, 13);
        matrixStack.translate(-2, 18, 0);
        int scale = 22;

        GuiGameElement.of(CABlocks.ROLLING_MILL.getDefaultState())
                .rotateBlock(22.5, 22.5, 0)
                .scale(scale)
                .render(matrixStack);

        matrixStack.popPose();
    }

}
