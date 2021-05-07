package com.alc.moreminecarts.blocks;

import com.alc.moreminecarts.tile_entities.ChunkLoaderTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class ChunkLoaderBlock extends ContainerBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ChunkLoaderTile tile_entity;

    public ChunkLoaderBlock(Properties builder) {
        super(builder);
        this.registerDefaultState(this.getStateDefinition().any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult trace_result) {
        if (world.isClientSide) return ActionResultType.SUCCESS;

        TileEntity tile_entity = world.getBlockEntity(pos);
        if (tile_entity instanceof ChunkLoaderTile) {
            NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tile_entity, pos);
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        tile_entity = new ChunkLoaderTile();
        return tile_entity;
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader reader) {
        return new ChunkLoaderTile();
    }

    // TODO Comparator stuff

}