package com.mrh0.createaddition.energy;

import java.util.List;

import com.mrh0.createaddition.transfer.EnergyTransferable;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public abstract class BaseElectricTileEntity extends SmartTileEntity implements EnergyTransferable {

	protected final InternalEnergyStorage energy;
	protected LazyOptional<EnergyStorage> lazyEnergy;
	
	private boolean firstTickState = true;
	protected final long CAPACITY, MAX_IN, MAX_OUT;
	
	public BaseElectricTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, long CAPACITY, long MAX_IN, long MAX_OUT) {
		super(tileEntityTypeIn, pos, state);
		energy = new InternalEnergyStorage(CAPACITY, MAX_IN, MAX_OUT);
		this.CAPACITY = CAPACITY;
		this.MAX_IN = MAX_IN;
		this.MAX_OUT = MAX_OUT;
		lazyEnergy = LazyOptional.of(() -> energy);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Nullable
	@Override
	public EnergyStorage getEnergyStorage(@Nullable Direction side) {
		if(isEnergyInput(side) || isEnergyOutput(side)) {
			return lazyEnergy.getValueUnsafer();
		}
		return null;
	}

	//	@Override
//	public <T> LazyOptional<T> getStorage(Capability<T> cap, Direction side) {
//		if(cap == CapabilityEnergy.ENERGY && (isEnergyInput(side) || isEnergyOutput(side)))// && !level.isClientSide
//			return lazyEnergy.cast();
//		return super.getStorage(cap, side);
//	}
	
	public abstract boolean isEnergyInput(Direction side);

	public abstract boolean isEnergyOutput(Direction side);
	
	
	@Override
	protected void read(CompoundTag compound, boolean arg1) {
		super.read(compound, arg1);
		energy.read(compound);
	}
	
	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		energy.write(compound);
	}
	
	@Override
	public void setRemoved() {
		super.setRemoved();
		lazyEnergy.invalidate();
	}
	
	@Deprecated
	public void outputTick(int max) {
		for(Direction side : Direction.values()) {
			if(!isEnergyOutput(side))
				continue;
			energy.outputToSide(level, worldPosition, side, max);
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		if(firstTickState)
			firstTick();
		firstTickState = false;
	}
	
	public void firstTick() {
		updateCache();
	};
	
	public void updateCache() {
		if(level.isClientSide())
			return;
		for(Direction side : Direction.values()) {
			BlockEntity te = level.getBlockEntity(worldPosition.relative(side));
			if(te == null) {
				setCache(side, LazyOptional.empty());
				continue;
			}
			LazyOptional<EnergyStorage> le = LazyOptional.ofObject(EnergyStorage.SIDED.find(te.getLevel(), te.getBlockPos(), side.getOpposite()));
			setCache(side, le);
		}
	}
	
	private LazyOptional<EnergyStorage> escacheUp = LazyOptional.empty();
	private LazyOptional<EnergyStorage> escacheDown = LazyOptional.empty();
	private LazyOptional<EnergyStorage> escacheNorth = LazyOptional.empty();
	private LazyOptional<EnergyStorage> escacheEast = LazyOptional.empty();
	private LazyOptional<EnergyStorage> escacheSouth = LazyOptional.empty();
	private LazyOptional<EnergyStorage> escacheWest = LazyOptional.empty();
	
	public void setCache(Direction side, LazyOptional<EnergyStorage> storage) {
		switch(side) {
			case DOWN:
				escacheDown = storage;
				break;
			case EAST:
				escacheEast = storage;
				break;
			case NORTH:
				escacheNorth = storage;
				break;
			case SOUTH:
				escacheSouth = storage;
				break;
			case UP:
				escacheUp = storage;
				break;
			case WEST:
				escacheWest = storage;
				break;
		}
	}
	
	public EnergyStorage getCachedEnergy(Direction side) {
		switch(side) {
			case DOWN:
				return escacheDown.orElse(null);
			case EAST:
				return escacheEast.orElse(null);
			case NORTH:
				return escacheNorth.orElse(null);
			case SOUTH:
				return escacheSouth.orElse(null);
			case UP:
				return escacheUp.orElse(null);
			case WEST:
				return escacheWest.orElse(null);
		}
		return null;
	}
	

	public boolean isValidUpgradeSide(BlockState state, Direction side) {
		return false;
	}
	
	public float getBoostPerUpgrade() {
		return 0f;
	}
}
