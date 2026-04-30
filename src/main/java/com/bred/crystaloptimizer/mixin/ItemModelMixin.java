package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.render.FakeSpawnerItemUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemRenderer.class, priority = 900)
public abstract class ItemModelMixin {

    private static final ModelIdentifier SPAWNER_MODEL =
        new ModelIdentifier(Identifier.of("minecraft", "spawner"), "inventory");

    @Inject(
        method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void exordium$renderFakeSpawner(ItemStack stack, ModelTransformationMode mode,
                                            boolean leftHanded, MatrixStack matrices,
                                            VertexConsumerProvider consumers, int light,
                                            int overlay, BakedModel model, CallbackInfo ci) {
        if (!FakeSpawnerItemUtil.shouldRenderAsSpawner(stack)) return;

        BakedModel spawnerModel = getSpawnerModel();
        if (spawnerModel == null || spawnerModel == model) return;

        ci.cancel();
        ((ItemRenderer) (Object) this).renderItem(
            new ItemStack(Items.SPAWNER),
            mode,
            leftHanded,
            matrices,
            consumers,
            light,
            overlay,
            spawnerModel
        );
    }

    @Inject(method = "getModel", at = @At("RETURN"), cancellable = true, require = 0)
    private void exordium$getFakeSpawnerModel(ItemStack stack, World world, LivingEntity entity,
                                              int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (!FakeSpawnerItemUtil.shouldRenderAsSpawner(stack)) return;

        BakedModel spawnerModel = getSpawnerModel();
        if (spawnerModel != null) {
            cir.setReturnValue(spawnerModel);
        }
    }

    private BakedModel getSpawnerModel() {
        BakedModelManager manager = MinecraftClient.getInstance().getBakedModelManager();
        BakedModel spawnerModel = manager.getModel(SPAWNER_MODEL);
        if (spawnerModel == null || spawnerModel == manager.getMissingModel()) {
            spawnerModel = ((ItemRenderer) (Object) this).getModel(new ItemStack(Items.SPAWNER), null, null, 0);
        }
        return spawnerModel == manager.getMissingModel() ? null : spawnerModel;
    }
}
