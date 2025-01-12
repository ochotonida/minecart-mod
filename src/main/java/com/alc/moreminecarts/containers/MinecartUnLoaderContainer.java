package com.alc.moreminecarts.containers;

import com.alc.moreminecarts.MMReferences;
import com.alc.moreminecarts.proxy.MoreMinecartsPacketHandler;
import com.alc.moreminecarts.tile_entities.AbstractCommonLoader;
import com.alc.moreminecarts.tile_entities.MinecartLoaderTile;
import com.alc.moreminecarts.tile_entities.MinecartUnloaderTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class MinecartUnLoaderContainer extends Container {

    private final AbstractCommonLoader tile;
    private final IInventory inventory;
    private final IIntArray data;
    protected final World level;

    public MinecartUnLoaderContainer(int n, World world, PlayerInventory player_inventory, PlayerEntity player_entity) {
        super(MMReferences.minecart_loader_c, n);

        this.tile = null;
        this.inventory = new Inventory(1);
        this.data = new IntArray(3);
        this.level = player_inventory.player.level;

        CommonInitialization(player_inventory);
    }

    // For use with tile entity loaders.
    public MinecartUnLoaderContainer(int n, World world, BlockPos pos, PlayerInventory player_inventory, PlayerEntity player_entity) {
        super(MMReferences.minecart_loader_c, n);

        TileEntity te = world.getBlockEntity(pos);

        if (te instanceof MinecartLoaderTile) {
            MinecartLoaderTile loader = (MinecartLoaderTile) te;
            this.inventory = loader;
            this.data = loader.dataAccess;
            this.tile = loader;
        } else if (te instanceof MinecartUnloaderTile) {
            MinecartUnloaderTile unloader = (MinecartUnloaderTile) te;
            this.inventory = unloader;
            this.data = unloader.dataAccess;
            this.tile = unloader;
        } else {
            // should error out?
            this.inventory = new Inventory(1);
            this.data = new IntArray(2);
            this.tile = null;
        }

        this.level = player_inventory.player.level;

        CommonInitialization(player_inventory);
    }

    public void CommonInitialization(PlayerInventory player_inventory) {

        checkContainerSize(inventory, 9);
        checkContainerDataCount(data, 2);

        // content slots
        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 42));
        }

        // player inventory slots, taken from the AbstractFurnaceContainer code.
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(player_inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(player_inventory, k, 8 + k * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return this.inventory.stillValid(player);
    }

    // Taken from the chest container. No clue what this does really.
    @Override
    public ItemStack quickMoveStack(PlayerEntity p_82846_1_, int p_82846_2_) {
        int containerRows = 1;
        ItemStack lvt_3_1_ = ItemStack.EMPTY;
        Slot lvt_4_1_ = (Slot)this.slots.get(p_82846_2_);
        if (lvt_4_1_ != null && lvt_4_1_.hasItem()) {
            ItemStack lvt_5_1_ = lvt_4_1_.getItem();
            lvt_3_1_ = lvt_5_1_.copy();
            if (p_82846_2_ < containerRows * 9) {
                if (!this.moveItemStackTo(lvt_5_1_, containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(lvt_5_1_, 0, containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (lvt_5_1_.isEmpty()) {
                lvt_4_1_.set(ItemStack.EMPTY);
            } else {
                lvt_4_1_.setChanged();
            }
        }

        return lvt_3_1_;
    }

    @OnlyIn(Dist.CLIENT)
    public int getSize() {
        return 9;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public FluidStack getFluids() {
        if (tile == null) return null;
        return tile.getFluidStack();
    }

    public int getEnergy() {
        if (tile == null) return 0;
        return tile.getEnergyAmount();
    }

    public boolean getRedstoneOutput()
    {
        return (this.data.get(0) & 16) == 16;
    }

    public boolean getLockedMinecartsOnly()
    {
        return this.data.get(0) % 2 == 1;
    }

    public boolean getLeaveOneInStack()
    {
        return (this.data.get(0) & 2) == 2;
    }

    public MinecartLoaderTile.ComparatorOutputType getComparatorOutputType()
    {
        return MinecartLoaderTile.ComparatorOutputType.fromInt((this.data.get(0) & 12) >> 2);
    }

    public MoreMinecartsPacketHandler.MinecartLoaderPacket getCurrentPacket() {
        return new MoreMinecartsPacketHandler.MinecartLoaderPacket(
                false,
                getLockedMinecartsOnly(),
                getLeaveOneInStack(),
                getComparatorOutputType(),
                getRedstoneOutput()
        );
    }

    public boolean getIsUnloader() {
        return this.data.get(1) > 0;
    }

    public void setOptions(boolean locked_minecarts_only, boolean leave_one_in_stack, MinecartLoaderTile.ComparatorOutputType comparator_output, boolean redstone_output) {
       int options = (locked_minecarts_only?1:0) + ((leave_one_in_stack?1:0) << 1) + (comparator_output.toInt() << 2) + ((redstone_output?1:0) << 4);
       this.setData(0, options);
       this.broadcastChanges();
    }
}
