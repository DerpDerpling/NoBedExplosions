package derp.nbe.mixin;

import derp.nbe.NBE;
import derp.nbe.config.ModConfig;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.minecraft.block.BedBlock.isBedWorking;

@Mixin(BedBlock.class)
public abstract class BedMixin extends Block {
	public BedMixin(Settings settings) {
		super(settings);
	}

	@Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
	private void onUseMixin(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
		if (!isBedWorking(world)) {
			if (!world.isClient) {
				// Check if it's nighttime before allowing the player to enter the bed
				if (!world.isDay()) {
					if (state.get(BedBlock.PART) != BedPart.HEAD) {
						pos = pos.offset(state.get(BedBlock.FACING));
						state = world.getBlockState(pos);
						if (!state.isOf(this)) {
							cir.setReturnValue(ActionResult.CONSUME);
							return;
						}
						if (world.getRegistryKey() != World.NETHER && world.getRegistryKey() != World.END) {
							world.removeBlock(pos, false);
							BlockPos blockPos = pos.offset(state.get(BedBlock.FACING).getOpposite());
							if (world.getBlockState(blockPos).isOf(this)) {
								world.removeBlock(blockPos, false);
							}
						}
						if (NBE.config.enabled) cir.setReturnValue(ActionResult.SUCCESS);
						if (NBE.config.enabled) player.sendMessage(Text.translatable("sleep.not_possible"), true);
					} else if (state.get(BedBlock.OCCUPIED)) {
						if (!this.wakeVillager(world, pos)) {
							player.sendMessage(Text.translatable("block.minecraft.bed.occupied"), true);
						}
					} else {
						{
							if (state.get(BedBlock.PART) != BedPart.FOOT) {
								state = world.getBlockState(pos);
								if (!state.isOf(this)) {
									cir.setReturnValue(ActionResult.CONSUME);
									return;
								}
								if (NBE.config.enabled) cir.setReturnValue(ActionResult.SUCCESS);
								if (NBE.config.enabled)
									player.sendMessage(Text.translatable("sleep.not_possible"), true);
							}
						}
					}
				}
			}
		}
	}


	@Unique
	private boolean wakeVillager(World world, BlockPos pos) {
		List<VillagerEntity> list = world.getEntitiesByClass(VillagerEntity.class, new Box(pos), LivingEntity::isSleeping);
		if (list.isEmpty()) {
			return false;
		} else {
			list.get(0).wakeUp();
			return true;
		}
	}

	@Override
	public boolean isEnabled(FeatureSet enabledFeatures) {
		return super.isEnabled(enabledFeatures);
	}
}
