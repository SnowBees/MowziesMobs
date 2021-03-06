package com.bobmowzie.mowziesmobs.server.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biomes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

/**
 * Created by Josh on 5/1/2017.
 */
public class MowzieBlockAccess implements IBlockReader {
    private BlockState accessState;
    private Biome biome = Biomes.PLAINS;

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getLightValue(BlockPos pos) {
        return 0;
    }

    public void setBlockState(BlockState state) {
        this.accessState = state;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return accessState;
    }

    @Override
    public IFluidState getFluidState(BlockPos blockPos) {
        return null;
    }

    /*@Override
    public boolean isAirBlock(BlockPos pos) {
        return false;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    public Biome getBiome() {
        return biome;
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return Minecraft.getInstance().world.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, Direction side, boolean _default) {
        return true;
    }*/ // TODO
}
