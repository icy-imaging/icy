/*
 * Copyright (c) 2010-2026. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.icy.common.collection.array;

import fr.icy.common.type.DataType;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fr.icy.common.EnforcerUtil.enforceGreaterOrEqual;
import static fr.icy.common.EnforcerUtil.enforceRange;

/**
 * Represents a dynamic array abstraction that adjusts its capacity as
 * elements are added or removed. The dynamic array manages memory allocation
 * using internal storage blocks and provides methods for manipulating and
 * interacting with the stored data.
 * <p>
 * The class supports customizable allocation granularity and different
 * data types. It internally uses an expandable collection of blocks to
 * optimize memory usage and performance.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public abstract class DynamicArray {
    /**
     * Creates a new instance of a DynamicArray with the specified data type and granularity.
     *
     * @param type        The data type of the dynamic array object. This defines the type of elements the array will store.
     * @param granularity A value between 0 and 8 that determines the memory allocation granularity.
     *                    Lower values represent less memory usage but higher allocation time, while higher
     *                    values represent more memory usage but lower allocation time. The default value is 4.
     * @return A new instance of a DynamicArray configured with the specified data type and granularity.
     * @throws NullPointerException      If the provided data type is {@code null}.
     * @throws IndexOutOfBoundsException If the specified granularity is outside the range [0, 8].
     */
    public static @NonNull DynamicArray create(final @NonNull DataType type, final @Range(from = 0, to = 8) int granularity) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(type, "Datatype cannot be null.");
        enforceRange(granularity, 0, 8, "Granularity must be between 0 and 8 (default = 4).");
        return switch (type.getJavaType()) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> new Byte(granularity);
            case SHORT, USHORT -> new Short();
            case INT, UINT -> new Int();
            case LONG, ULONG -> new Long();
            case FLOAT -> new Float();
            case DOUBLE -> new Double();
            //default -> null;
        };
    }

    /**
     * Creates a new instance of a DynamicArray with the specified data type and granularity.
     *
     * @param type The data type of the dynamic array object. This defines the type of elements the array will store.
     * @return A new instance of a DynamicArray configured with the specified data type and granularity.
     * @throws NullPointerException If the provided data type is {@code null}.
     */
    public static @NonNull DynamicArray create(final @NonNull DataType type) throws NullPointerException {
        return create(type, 4);
    }

    /**
     * Defines the size of each memory block used for data storage within the dynamic array.
     * The value of this variable must always be a power of 2, which ensures efficient computation
     * of memory offsets and alignment during data management operations. This also allows
     * quick bitwise calculations for block-related tasks.
     * <p>
     * The block size directly influences the performance and memory consumption of the
     * dynamic array. Larger block sizes may reduce the number of allocations required
     * at the cost of potentially higher memory usage, whereas smaller block sizes may
     * increase allocation frequency but reduce memory overhead.
     * <p>
     * This value is immutable once set and plays a crucial role in determining the behavior
     * of the dynamic array's internal data structure.
     */
    private final int blockSize;
    /**
     * Stores the collection of array blocks that make up the dynamic array.
     * Each block represents a contiguous region of memory allocated to hold
     * a portion of the array's elements. The blocks are managed dynamically
     * to accommodate resizing operations and enable efficient memory utilization.
     * <p>
     * This list is immutable in terms of structure, but the individual
     * {@code ArrayBlock} instances within it may be modified as required.
     */
    private final @NonNull List<@NonNull ArrayBlock> blocks;

    /**
     * Constructs a new DynamicArray with the specified memory allocation granularity.
     *
     * @param granularity A value between 0 and 8 that determines the memory allocation granularity.
     *                    Lower values represent less memory usage but higher allocation time, while
     *                    higher values represent more memory usage but lower allocation time.
     * @throws IndexOutOfBoundsException If the specified granularity is outside the range [0, 8].
     */
    DynamicArray(final @Range(from = 0, to = 8) int granularity) throws IndexOutOfBoundsException {
        super();

        enforceRange(granularity, 0, 8, "Granularity must be between 0 and 8.");

        blockSize = 1 << (8 + granularity);
        blocks = new ArrayList<>();
    }

    /**
     * Constructs a new DynamicArray with a default memory allocation granularity of 4.
     * This constructor delegates to the more specific constructor that allows specifying
     * the granularity.
     */
    DynamicArray() {
        this(4);
    }

    /**
     * Creates a new array with the specified size.
     *
     * @param size The size of the array to be created. Must be non-negative.
     * @return A newly created array object of the specified size.
     * @throws IndexOutOfBoundsException If the specified size is negative.
     */
    protected abstract @NonNull Object createArray(@Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException;

    /**
     * Retrieves the size of the given array object.
     *
     * @param array The array object whose size is to be determined. Must not be {@code null}.
     * @return The size of the array as a non-negative integer.
     * @throws NullPointerException     If the provided object is {@code null}.
     * @throws IllegalArgumentException If the provided object is not a valid array.
     */
    protected abstract @Range(from = 0, to = Integer.MAX_VALUE) int getArraySize(@NonNull Object array) throws NullPointerException, IllegalArgumentException;

    /**
     * Clears all elements from the dynamic array, resetting its size to zero.
     * <p>
     * This method removes all internal storage blocks and sets the size of the array to zero.
     * After calling this method, the dynamic array will be empty.
     */
    public void clear() {
        setSize(0);
    }

    /**
     * Checks if the dynamic array is empty.
     * <p>
     * This method determines whether the dynamic array contains any elements
     * by comparing its size to zero.
     *
     * @return {@code true} if the dynamic array has no elements, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return getSize() == 0;
    }

    /**
     * Adds a new instance of ArrayBlock to the internal collection of blocks and returns it.
     * <p>
     * The newly created ArrayBlock is added to the internal blocks collection that manages
     * the storage units of the dynamic array.
     *
     * @return The newly created ArrayBlock instance that has been added to the collection.
     */
    protected ArrayBlock addBlock() {
        final ArrayBlock result = new ArrayBlock();

        blocks.add(result);

        return result;
    }

    /**
     * Removes the last block from the internal collection of blocks in the dynamic array.
     * <p>
     * This method checks if there are any blocks currently stored in the dynamic array.
     * If one or more blocks exist, the last block is removed from the collection,
     * reducing the storage capacity by one block. If no blocks are present, no action is taken.
     */
    protected void removeBlock() {
        final int numBlock = blocks.size();

        // remove last block if it exists
        if (numBlock > 0)
            blocks.remove(numBlock - 1);
    }

    /**
     * Ensures that the dynamic array has enough capacity to accommodate the specified size.
     * If the current capacity is less than the required size, the method adjusts the array's
     * capacity by increasing its size.
     *
     * @param size The required size of the dynamic array. Must be a non-negative integer.
     * @throws IndexOutOfBoundsException If the specified size is negative.
     */
    protected void checkCapacity(final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
        while (getCapacity() < size)
            setSize(size);
    }

    /**
     * Retrieves the total capacity of the dynamic array.
     * <p>
     * The capacity is calculated as the product of the number of blocks
     * and the size of each block. It represents the maximum number of
     * elements that the array can hold without requiring additional
     * memory allocation.
     *
     * @return The total capacity of the dynamic array as a non-negative integer.
     */
    public @Range(from = 0, to = Integer.MAX_VALUE) int getCapacity() {
        return blocks.size() * blockSize;
    }

    /**
     * Retrieves the index of the last block in the internal collection of blocks.
     * This method calculates the last block's index by subtracting 1 from the
     * total number of blocks in the collection.
     *
     * @return The zero-based index of the last block as an integer. If the collection
     * of blocks is empty, this method returns -1.
     */
    protected @Range(from = -1, to = Integer.MAX_VALUE) int getLastBlockIndex() {
        return blocks.size() - 1;
    }

    /**
     * Retrieves the current size of the dynamic array.
     * <p>
     * The size is calculated based on the number of blocks in the array and
     * the sizes of the elements within the blocks. If the dynamic array
     * contains no blocks, the size returned is zero.
     *
     * @return The size of the dynamic array as a non-negative integer.
     */
    public @Range(from = 0, to = Integer.MAX_VALUE) int getSize() {
        final int lastBlockIndex = getLastBlockIndex();

        if (lastBlockIndex < 0)
            return 0;

        return (blockSize * lastBlockIndex) + blocks.get(lastBlockIndex).getSize();
    }

    /**
     * Retrieves the last block in the collection if it exists.
     *
     * @return the last block in the collection, or {@code null} if no blocks exist.
     * @throws IndexOutOfBoundsException if an invalid block index is accessed.
     */
    protected @Nullable ArrayBlock getLastBlock() throws IndexOutOfBoundsException {
        final int lastBlockIndex = getLastBlockIndex();

        if (lastBlockIndex < 0)
            return null;

        return blocks.get(lastBlockIndex);
    }

    /**
     * Retrieves the block corresponding to the specified offset.
     *
     * @param offset the zero-based offset used to locate the desired block.
     *               Must be a non-negative integer within a valid range.
     * @return the block corresponding to the given offset.
     * @throws IndexOutOfBoundsException if the computed block index is outside the bounds of available blocks.
     */
    protected @NonNull ArrayBlock getBlockFromOffset(final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");

        final int blockIndex = offset / blockSize;

        if (blockIndex < blocks.size())
            return blocks.get(blockIndex);

        throw new IndexOutOfBoundsException("Invalid block index: " + blockIndex);
    }

    /**
     * Retrieves the last available block with free space. Returns {@code null} if no suitable block is found.
     *
     * @return the last block with available space, or {@code null} if no suitable block is found.
     */
    protected @Nullable ArrayBlock getAvailableBlock() {
        final ArrayBlock lastBlock = getLastBlock();

        // last block exists and has free space? --> return it
        if ((lastBlock != null) && (lastBlock.getAvailable() > 0))
            return lastBlock;

        return null;
    }

    /**
     * Retrieves the last available block with free space. A new block
     * is created and returned if no suitable block is found.
     *
     * @return the last block with available space, or a newly created block if no suitable block is found.
     */
    protected @NonNull ArrayBlock getAvailableBlockOrCreate() {
        final ArrayBlock lastBlock = getAvailableBlock();

        if (lastBlock == null)
            return addBlock();

        return lastBlock;
    }

    /**
     * Sets the size of the dynamic array, adjusting its internal storage blocks accordingly.
     * <p>
     * If the specified size is 0, all internal storage blocks are cleared. If the size is
     * greater than the current capacity, additional blocks are allocated and initialized
     * to meet the required size. If the size is less than the current capacity, excess blocks
     * are removed, and the last block is resized if necessary.
     *
     * @param size The desired size of the dynamic array. Must be a non-negative integer.
     * @throws IndexOutOfBoundsException If the specified size is negative.
     */
    public void setSize(final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
        // special case
        if (size == 0) {
            blocks.clear();
            return;
        }

        // add blocks if needed
        while (getCapacity() < size) {
            final ArrayBlock block = addBlock();
            // set block size
            block.size = blockSize;
        }
        // remove blocks if needed
        while ((getCapacity() - blockSize) > size)
            removeBlock();

        // adjust last block size if needed
        if (getCapacity() > size) {
            final ArrayBlock last = getLastBlock();
            if (last != null)
                last.size = getCapacity() - size;
        }
    }

    /**
     * Adds a portion of the input object to available blocks while respecting the specified offset and length.
     *
     * @param in       the input object to add; must not be {@code null}.
     * @param inOffset the starting offset within the input object; must be a non-negative integer.
     * @param len      the number of elements to add; must be a non-negative integer.
     * @throws NullPointerException      if the input object is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the input offset is out of bounds or length is negative.
     * @throws ArrayStoreException       if there is a type mismatch between the elements of the input array and the internal array.
     */
    public void add(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException, ArrayStoreException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        enforceGreaterOrEqual(inOffset, 0, "Input offset cannot be negative.");
        enforceGreaterOrEqual(len, 0, "Input length cannot be negative.");
        int offset = inOffset;
        int cnt = len;
        while (cnt > 0) {
            final ArrayBlock block = getAvailableBlockOrCreate();
            final int blockSpace = block.getAvailable();
            final int toCopy = Math.min(blockSpace, cnt);

            block.add(in, offset, toCopy);
            offset += toCopy;
            cnt -= toCopy;
        }
    }

    /**
     * Adds the specified object to the target collection or structure.
     *
     * @param in the object to be added; must not be {@code null}.
     * @throws NullPointerException     if the input object is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an array.
     * @throws ArrayStoreException      if there is a type mismatch between the elements of the input array and the internal array.
     */
    public void add(final @NonNull Object in) throws NullPointerException, IllegalArgumentException, ArrayStoreException {
        add(in, 0, getArraySize(in));
    }

    /**
     * Adds all elements from the specified dynamic array to the current collection.
     *
     * @param in the dynamic array containing elements to be added; must not be {@code null}.
     * @throws NullPointerException if the input object is {@code null}.
     * @throws ArrayStoreException  if there is a type mismatch between the elements of the input array and the internal array.
     */
    public void addAll(final @NonNull DynamicArray in) throws NullPointerException, ArrayStoreException {
        Objects.requireNonNull(in, "Dynamic array cannot be null.");
        final Object array = in.asArray();

        add(array, 0, getArraySize(array));
    }

    /**
     * Retrieves data from the source starting at the specified input offset and copies it into the
     * provided output object starting at the specified output offset, up to the specified length.
     *
     * @param out       The output object where data will be copied. This parameter must be non-null.
     * @param inOffset  The starting offset in the source from where data will be read. Must be greater
     *                  than or equal to 0.
     * @param outOffset The starting offset in the output object where data will be written. Must be
     *                  greater than or equal to 0.
     * @param len       The number of elements to copy from the source to the output. Must be greater than or
     *                  equal to 0.
     * @throws NullPointerException      if the output array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the specified offset or length exceed the bounds of the input or output arrays.
     * @throws ArrayStoreException       if there is a type mismatch between the elements of the input array and the internal array.
     */
    public void get(final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException, ArrayStoreException {
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(inOffset, 0, "Input offset cannot be negative.");
        enforceGreaterOrEqual(outOffset, 0, "Output offset cannot be negative.");
        enforceGreaterOrEqual(len, 0, "Input length cannot be negative.");
        int srcOffset = inOffset;
        int dstOffset = outOffset;
        int cnt = len;
        while (cnt > 0) {
            final ArrayBlock block = getBlockFromOffset(srcOffset);
            final int subSrcOffset = srcOffset & (blockSize - 1);
            final int subLen = Math.min(blockSize - subSrcOffset, cnt);

            block.get(out, subSrcOffset, dstOffset, subLen);
            srcOffset += subLen;
            dstOffset += subLen;
            cnt -= subLen;
        }
    }

    /**
     * Transfers a portion of data from the input source to the internal structure starting at specific offsets.
     * Data is written in chunks depending on block size.
     *
     * @param in        The input source from which the data is to be transferred. Cannot be {@code  null}.
     * @param inOffset  The starting offset in the input source. Must be greater than or equal to 0.
     * @param outOffset The starting offset in the internal structure where data will be transferred. Must be greater than or equal to 0.
     * @param len       The number of elements to transfer. Must be greater than or equal to 0.
     * @throws NullPointerException      if the input array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the specified offset or length exceed the bounds of the input or output arrays.
     * @throws ArrayStoreException       if there is a type mismatch between the elements of the input array and the internal array.
     */
    public void put(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException, ArrayStoreException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        enforceGreaterOrEqual(inOffset, 0, "Input offset cannot be negative.");
        enforceGreaterOrEqual(outOffset, 0, "Output offset cannot be negative.");
        enforceGreaterOrEqual(len, 0, "Input length cannot be negative.");
        checkCapacity(outOffset + len);

        int srcOffset = inOffset;
        int dstOffset = outOffset;
        int cnt = len;
        while (cnt > 0) {
            final ArrayBlock block = getBlockFromOffset(dstOffset);
            final int subDstOffset = dstOffset & (blockSize - 1);
            final int subLen = Math.min(blockSize - subDstOffset, cnt);

            block.put(in, srcOffset, subDstOffset, subLen);
            srcOffset += subLen;
            dstOffset += subLen;
            cnt -= subLen;
        }
    }

    /**
     * Converts the current set of blocks into a single array.
     * This method aggregates data from multiple blocks, combining them sequentially
     * into a single array representation.
     *
     * @return a non-null object representing the aggregated array created from the blocks.
     */
    public @NonNull Object asArray() {
        final Object result = createArray(getSize());

        int offset = 0;
        for (final ArrayBlock block : blocks) {
            final int blockSize = block.getSize();
            block.get(result, 0, offset, blockSize);
            offset += blockSize;
        }

        return result;
    }

    /**
     * The {@code ArrayBlock} class provides a data structure for managing blocks of
     * elements within an array. It includes methods for adding, retrieving, and
     * manipulating elements in a block-based manner.
     */
    protected class ArrayBlock {
        protected Object array;
        protected int size;

        /**
         * Constructs an instance of the {@code ArrayBlock} class.
         * Initializes the internal array with the specified block size
         * using the {@link #createArray(int)} method and sets the initial
         * size of the block to zero.
         */
        public ArrayBlock() {
            super();

            array = createArray(blockSize);
            size = 0;
        }

        /**
         * Resets the current size of the block to zero, effectively clearing its content.
         * This method affects the size tracking variable but does not modify the internal array data.
         */
        protected void clear() {
            size = 0;
        }

        /**
         * Retrieves the current size of the block.
         *
         * @return the current size of the block, represented as an integer.
         */
        public int getSize() {
            return size;
        }

        /**
         * Calculates and returns the remaining available capacity in the block.
         * The available capacity is determined as the difference between the
         * block's total size and the current size.
         *
         * @return the number of available slots in the block.
         */
        public int getAvailable() {
            return blockSize - getSize();
        }

        /**
         * Copies elements from an internal array to an external output array based on specified offsets and length.
         * Validates parameters to ensure non-nullity, non-negativity, and bounds are within valid ranges.
         *
         * @param out       the output array where elements will be copied.
         * @param inOffset  the starting index in the internal array from which elements are copied.
         * @param outOffset the starting index in the output array where elements will be placed.
         * @param len       the number of elements to copy.
         * @throws NullPointerException      if the output array is {@code null}.
         * @throws IllegalArgumentException  if the provided object is not an array.
         * @throws IndexOutOfBoundsException if the specified offset or length exceed the bounds of the input or output arrays.
         * @throws ArrayStoreException       if there is a type mismatch between the elements of the input array and the internal array.
         */
        @SuppressWarnings("SuspiciousSystemArraycopy")
        protected void get(final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException, ArrayStoreException {
            Objects.requireNonNull(out, "Input array cannot be null.");
            enforceGreaterOrEqual(inOffset, 0, "Input offset cannot be negative.");
            Objects.requireNonNull(array, "Output array cannot be null.");
            enforceGreaterOrEqual(outOffset, 0, "Output offset cannot be negative.");
            enforceGreaterOrEqual(len, 0, "Length cannot be negative.");

            if (!out.getClass().isArray())
                throw new IllegalArgumentException("Output array must be an array.");

            System.arraycopy(array, inOffset, out, outOffset, len);
        }

        /**
         * Copies elements from the input array to the internal array, starting at a specified offset
         * in the input array and for a specified length. The method validates the input parameters
         * and ensures the operations are within bounds.
         *
         * @param in       the input array from which elements are copied.
         * @param inOffset the starting index in the input array from which the elements should be copied.
         * @param len      the number of elements to copy from the input array.
         * @throws NullPointerException      if the input array is {@code null}.
         * @throws IllegalArgumentException  if the provided object is not an array.
         * @throws IndexOutOfBoundsException if the specified offset or length exceed the bounds of the input or output arrays.
         * @throws ArrayStoreException       if there is a type mismatch between the elements of the input array and the internal array.
         */
        @SuppressWarnings("SuspiciousSystemArraycopy")
        protected void add(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException, ArrayStoreException {
            Objects.requireNonNull(in, "Input array cannot be null.");
            enforceGreaterOrEqual(inOffset, 0, "Input offset cannot be negative.");
            Objects.requireNonNull(array, "Output array cannot be null.");
            enforceGreaterOrEqual(len, 0, "Length cannot be negative.");

            if (!in.getClass().isArray())
                throw new IllegalArgumentException("Input is not an array");

            System.arraycopy(in, inOffset, array, size, len);
            size += len;
        }

        /**
         * Copies elements from the input array to the internal output array, starting at specified offsets and for a specified length.
         * The method ensures that the input and output arrays are not null and enforces the bounds for the arrays.
         *
         * @param in        the input array from which elements are copied.
         * @param inOffset  the starting offset in the input array.
         * @param outOffset the starting offset in the internal output array.
         * @param len       the number of elements to copy.
         * @throws NullPointerException      if the input array is {@code null}.
         * @throws IllegalArgumentException  if the provided object is not an array.
         * @throws IndexOutOfBoundsException if the specified offset or length exceed the bounds of the input or output arrays.
         * @throws ArrayStoreException       if there is a type mismatch between the elements of the input array and the internal array.
         */
        @SuppressWarnings("SuspiciousSystemArraycopy")
        protected void put(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException, ArrayStoreException {
            Objects.requireNonNull(in, "Input array cannot be null.");
            enforceGreaterOrEqual(inOffset, 0, "Input offset cannot be negative.");
            Objects.requireNonNull(array, "Output array cannot be null.");
            enforceGreaterOrEqual(outOffset, 0, "Output offset cannot be negative.");
            enforceGreaterOrEqual(len, 0, "Length cannot be negative.");

            if (!in.getClass().isArray())
                throw new IllegalArgumentException("Provided object is not an array.");

            System.arraycopy(in, inOffset, array, outOffset, len);
            size = Math.max(size, outOffset + len);
        }
    }

    /**
     * The {@code Generic} class extends the {@code DynamicArray} class to provide
     * functionality specifically designed for handling objects in a dynamic array structure.
     * This implementation ensures type safety and additional operations for managing
     * object-based arrays.
     */
    public static class Generic extends DynamicArray {
        /**
         * Adds a single non-null object to the dynamic array.
         *
         * @param value the object to be added; must not be {@code null}.
         * @throws NullPointerException if {@code value} is {@code null}.
         */
        public void addSingle(final @NonNull Object value) throws NullPointerException {
            Objects.requireNonNull(value, "Value cannot be null.");
            final ArrayBlock block = getAvailableBlockOrCreate();
            ((Object[]) block.array)[block.size++] = value;
        }

        /**
         * Creates and returns a new array of {@link Object} with the specified size.
         *
         * @param size the size of the array to be created; must be a non-negative integer.
         * @return a newly allocated array of {@link Object} with the specified size.
         * @throws IndexOutOfBoundsException if the size is negative.
         */
        @Override
        protected @NonNull Object createArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException {
            enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
            return new Object[size];
        }

        /**
         * Retrieves the size of the specified array.
         *
         * @param array the array whose size is to be determined; must be a non-null {@link Object} array.
         * @return the number of elements in the specified array.
         * @throws NullPointerException if the provided array is {@code null}.
         * @throws NullPointerException if the provided object is not an array.
         */
        @Override
        protected @Range(from = 0, to = Integer.MAX_VALUE) int getArraySize(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
            Objects.requireNonNull(array, "Array cannot be null.");
            if (array.getClass().isArray())
                return ((Object[]) array).length;
            else throw new IllegalArgumentException("Provided array is not an Object array.");
        }

        /**
         * Converts the current set of blocks into a single array and returns it.
         * This method overrides the {@code asArray} implementation to ensure the result
         * is explicitly cast to an {@code Object[]} type.
         *
         * @return a non-null array of {@link Object} combining data from all blocks.
         */
        @Override
        public @NonNull Object @NonNull [] asArray() {
            return (Object[]) super.asArray();
        }
    }

    /**
     * A specialized dynamic array implementation to manage byte values.
     * This class extends the {@code DynamicArray} to provide functionality
     * specific to handling an array of bytes with dynamic allocation.
     * <p>
     * The {@code Byte} class is designed for scenarios where the size of the byte array
     * is not fixed, and dynamic management is required. It supports operations
     * like adding byte values, creating arrays of specified sizes, and
     * converting the data into a contiguous byte array.
     */
    public static class Byte extends DynamicArray {
        /**
         * Constructs a new {@code Byte} instance with the specified granularity for memory allocation.
         * The granularity defines the size of memory blocks allocated dynamically for the array.
         *
         * @param granularity a value between 0 and 8 that determines memory allocation granularity.
         *                    Lower values represent less memory usage but higher allocation time, while
         *                    higher values represent more memory usage but lower allocation time.
         * @throws IndexOutOfBoundsException if the specified granularity is outside the range [0, 8].
         */
        public Byte(final int granularity) throws IndexOutOfBoundsException {
            super(granularity);
        }

        /**
         * Constructs a new {@code Byte} instance with default properties.
         * This no-argument constructor initializes the parent {@code DynamicArray}
         * with its default configuration by invoking its no-argument constructor.
         * <p>
         * This class is primarily used to manage and manipulate dynamic arrays of bytes with
         * features like adding elements dynamically, creating arrays, and converting the data
         * to a contiguous byte array.
         */
        public Byte() {
            super();
        }

        /**
         * Adds a single byte value to the array.
         * If no available block exists, a new block is created to accommodate the value.
         *
         * @param value the byte value to be added to the array.
         */
        public void addSingle(final byte value) {
            final ArrayBlock block = getAvailableBlockOrCreate();
            ((byte[]) block.array)[block.size++] = value;
        }

        /**
         * Creates a new array with the specified size.
         * The array created will have a capacity equal to the provided size.
         *
         * @param size the number of elements the array should be able to hold; must be a non-negative integer.
         * @return a new byte array with the specified size.
         * @throws IndexOutOfBoundsException if the specified size is negative.
         */
        @Override
        protected @NonNull Object createArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException {
            enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
            return new byte[size];
        }

        /**
         * Retrieves the size of the specified array.
         * The array must be a non-null byte array.
         *
         * @param array the non-null array whose size is to be determined, must be of type {@code byte[]}.
         * @return the size of the array (number of elements it contains).
         * @throws NullPointerException if the input array is {@code null}.
         */
        @Override
        protected @Range(from = 0, to = Integer.MAX_VALUE) int getArraySize(final @NonNull Object array) throws NullPointerException {
            Objects.requireNonNull(array, "Array cannot be null.");
            return ((byte[]) array).length;
        }

        /**
         * Converts the current set of blocks in the byte array into a single aggregated array.
         * This method combines the underlying blocks of the dynamic byte array into a
         * contiguous array representation.
         *
         * @return a non-null byte array containing the combined data from all blocks.
         */
        @Override
        public byte @NonNull [] asArray() {
            return (byte[]) super.asArray();
        }
    }

    /**
     * A specialized dynamic array implementation to manage short values.
     * This class extends the {@code DynamicArray} to provide functionality
     * specific to handling an array of shorts with dynamic allocation.
     * <p>
     * The {@code Short} class is designed for scenarios where the size of the short array
     * is not fixed, and dynamic management is required. It supports operations
     * like adding short values, creating arrays of specified sizes, and
     * converting the data into a contiguous short array.
     */
    public static class Short extends DynamicArray {
        /**
         * Constructs a new {@code Short} instance with the specified granularity for memory allocation.
         * The granularity defines the size of memory blocks allocated dynamically for the array.
         *
         * @param granularity a value between 0 and 8 that determines memory allocation granularity.
         *                    Lower values represent less memory usage but higher allocation time, while
         *                    higher values represent more memory usage but lower allocation time.
         * @throws IndexOutOfBoundsException if the specified granularity is outside the range [0, 8].
         */
        public Short(final int granularity) throws IndexOutOfBoundsException {
            super(granularity);
        }

        /**
         * Constructs a new {@code Short} instance with default properties.
         * This no-argument constructor initializes the parent {@code DynamicArray}
         * with its default configuration by invoking its no-argument constructor.
         * <p>
         * This class is primarily used to manage and manipulate dynamic arrays of shorts with
         * features like adding elements dynamically, creating arrays, and converting the data
         * to a contiguous short array.
         */
        public Short() {
            super();
        }

        /**
         * Adds a single short value to the array.
         * If no available block exists, a new block is created to accommodate the value.
         *
         * @param value the short value to be added to the array
         */
        public void addSingle(final short value) {
            final ArrayBlock block = getAvailableBlockOrCreate();
            ((short[]) block.array)[block.size++] = value;
        }

        /**
         * Creates a new array with the specified size.
         * The array created will have a capacity equal to the provided size.
         *
         * @param size the number of elements the array should be able to hold; must be a non-negative integer.
         * @return a new short array with the specified size.
         * @throws IndexOutOfBoundsException if the specified size is negative.
         */
        @Override
        protected @NonNull Object createArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException {
            enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
            return new short[size];
        }

        /**
         * Retrieves the size of the specified array.
         * The array must be a non-null short array.
         *
         * @param array the non-null array whose size is to be determined, must be of type {@code short[]}.
         * @return the size of the array (number of elements it contains).
         * @throws NullPointerException if the input array is {@code null}.
         */
        @Override
        protected @Range(from = 0, to = Integer.MAX_VALUE) int getArraySize(final @NonNull Object array) throws NullPointerException {
            Objects.requireNonNull(array, "Array cannot be null.");
            return ((short[]) array).length;
        }

        /**
         * Converts the current set of blocks in the short array into a single aggregated array.
         * This method combines the underlying blocks of the dynamic short array into a
         * contiguous array representation.
         *
         * @return a non-null short array containing the combined data from all blocks.
         */
        @Override
        public short @NonNull [] asArray() {
            return (short[]) super.asArray();
        }
    }

    /**
     * A specialized dynamic array implementation to manage int values.
     * This class extends the {@code DynamicArray} to provide functionality
     * specific to handling an array of ints with dynamic allocation.
     * <p>
     * The {@code Int} class is designed for scenarios where the size of the int array
     * is not fixed, and dynamic management is required. It supports operations
     * like adding int values, creating arrays of specified sizes, and
     * converting the data into a contiguous int array.
     */
    public static class Int extends DynamicArray {
        /**
         * Constructs a new {@code Int} instance with the specified granularity for memory allocation.
         * The granularity defines the size of memory blocks allocated dynamically for the array.
         *
         * @param granularity a value between 0 and 8 that determines memory allocation granularity.
         *                    Lower values represent less memory usage but higher allocation time, while
         *                    higher values represent more memory usage but lower allocation time.
         * @throws IndexOutOfBoundsException if the specified granularity is outside the range [0, 8].
         */
        public Int(final int granularity) throws IndexOutOfBoundsException {
            super(granularity);
        }

        /**
         * Constructs a new {@code Int} instance with default properties.
         * This no-argument constructor initializes the parent {@code DynamicArray}
         * with its default configuration by invoking its no-argument constructor.
         * <p>
         * This class is primarily used to manage and manipulate dynamic arrays of ints with
         * features like adding elements dynamically, creating arrays, and converting the data
         * to a contiguous int array.
         */
        public Int() {
            super();
        }

        /**
         * Adds a single int value to the array.
         * If no available block exists, a new block is created to accommodate the value.
         *
         * @param value the int value to be added to the array.
         */
        public void addSingle(final int value) {
            final ArrayBlock block = getAvailableBlockOrCreate();
            ((int[]) block.array)[block.size++] = value;
        }

        /**
         * Creates a new array with the specified size.
         * The array created will have a capacity equal to the provided size.
         *
         * @param size the number of elements the array should be able to hold; must be a non-negative integer.
         * @return a new int array with the specified size.
         * @throws IndexOutOfBoundsException if the specified size is negative.
         */
        @Override
        protected @NonNull Object createArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException {
            enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
            return new int[size];
        }

        /**
         * Retrieves the size of the specified array.
         * The array must be a non-null int array.
         *
         * @param array the non-null array whose size is to be determined, must be of type {@code int[]}.
         * @return the size of the array (number of elements it contains).
         * @throws NullPointerException if the input array is {@code null}.
         */
        @Override
        protected @Range(from = 0, to = Integer.MAX_VALUE) int getArraySize(final @NonNull Object array) throws NullPointerException {
            Objects.requireNonNull(array, "Array cannot be null.");
            return ((int[]) array).length;
        }

        /**
         * Converts the current set of blocks in the int array into a single aggregated array.
         * This method combines the underlying blocks of the dynamic int array into a
         * contiguous array representation.
         *
         * @return a non-null int array containing the combined data from all blocks.
         */
        @Override
        public int @NonNull [] asArray() {
            return (int[]) super.asArray();
        }
    }

    /**
     * A specialized dynamic array implementation to manage long values.
     * This class extends the {@code DynamicArray} to provide functionality
     * specific to handling an array of longs with dynamic allocation.
     * <p>
     * The {@code Long} class is designed for scenarios where the size of the long array
     * is not fixed, and dynamic management is required. It supports operations
     * like adding long values, creating arrays of specified sizes, and
     * converting the data into a contiguous long array.
     */
    public static class Long extends DynamicArray {
        /**
         * Constructs a new {@code Long} instance with the specified granularity for memory allocation.
         * The granularity defines the size of memory blocks allocated dynamically for the array.
         *
         * @param granularity a value between 0 and 8 that determines memory allocation granularity.
         *                    Lower values represent less memory usage but higher allocation time, while
         *                    higher values represent more memory usage but lower allocation time.
         * @throws IndexOutOfBoundsException if the specified granularity is outside the range [0, 8].
         */
        public Long(final int granularity) throws IndexOutOfBoundsException {
            super(granularity);
        }

        /**
         * Constructs a new {@code Long} instance with default properties.
         * This no-argument constructor initializes the parent {@code DynamicArray}
         * with its default configuration by invoking its no-argument constructor.
         * <p>
         * This class is primarily used to manage and manipulate dynamic arrays of longs with
         * features like adding elements dynamically, creating arrays, and converting the data
         * to a contiguous long array.
         */
        public Long() {
            super();
        }

        /**
         * Adds a single long value to the array.
         * If no available block exists, a new block is created to accommodate the value.
         *
         * @param value the long value to be added to the array.
         */
        public void addSingle(final long value) {
            final ArrayBlock block = getAvailableBlockOrCreate();
            ((long[]) block.array)[block.size++] = value;
        }

        /**
         * Creates a new array with the specified size.
         * The array created will have a capacity equal to the provided size.
         *
         * @param size the number of elements the array should be able to hold; must be a non-negative integer.
         * @return a new long array with the specified size.
         * @throws IndexOutOfBoundsException if the specified size is negative.
         */
        @Override
        protected @NonNull Object createArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException {
            enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
            return new long[size];
        }

        /**
         * Retrieves the size of the specified array.
         * The array must be a non-null long array.
         *
         * @param array the non-null array whose size is to be determined, must be of type {@code long[]}.
         * @return the size of the array (number of elements it contains).
         * @throws NullPointerException if the input array is {@code null}.
         */
        @Override
        protected @Range(from = 0, to = Integer.MAX_VALUE) int getArraySize(final @NonNull Object array) throws NullPointerException {
            Objects.requireNonNull(array, "Array cannot be null.");
            return ((long[]) array).length;
        }

        /**
         * Converts the current set of blocks in the long array into a single aggregated array.
         * This method combines the underlying blocks of the dynamic long array into a
         * contiguous array representation.
         *
         * @return a non-null long array containing the combined data from all blocks.
         */
        @Override
        public long @NonNull [] asArray() {
            return (long[]) super.asArray();
        }
    }

    /**
     * A specialized dynamic array implementation to manage float values.
     * This class extends the {@code DynamicArray} to provide functionality
     * specific to handling an array of floats with dynamic allocation.
     * <p>
     * The {@code Float} class is designed for scenarios where the size of the float array
     * is not fixed, and dynamic management is required. It supports operations
     * like adding float values, creating arrays of specified sizes, and
     * converting the data into a contiguous float array.
     */
    public static class Float extends DynamicArray {
        /**
         * Constructs a new {@code Float} instance with the specified granularity for memory allocation.
         * The granularity defines the size of memory blocks allocated dynamically for the array.
         *
         * @param granularity a value between 0 and 8 that determines memory allocation granularity.
         *                    Lower values represent less memory usage but higher allocation time, while
         *                    higher values represent more memory usage but lower allocation time.
         * @throws IndexOutOfBoundsException if the specified granularity is outside the range [0, 8].
         */
        public Float(final int granularity) throws IndexOutOfBoundsException {
            super(granularity);
        }

        /**
         * Constructs a new {@code Float} instance with default properties.
         * This no-argument constructor initializes the parent {@code DynamicArray}
         * with its default configuration by invoking its no-argument constructor.
         * <p>
         * This class is primarily used to manage and manipulate dynamic arrays of floats with
         * features like adding elements dynamically, creating arrays, and converting the data
         * to a contiguous float array.
         */
        public Float() {
            super();
        }

        /**
         * Adds a single float value to the array.
         * If no available block exists, a new block is created to accommodate the value.
         *
         * @param value the float value to be added to the array.
         */
        public void addSingle(final float value) {
            final ArrayBlock block = getAvailableBlockOrCreate();
            ((float[]) block.array)[block.size++] = value;
        }

        /**
         * Creates a new array with the specified size.
         * The array created will have a capacity equal to the provided size.
         *
         * @param size the number of elements the array should be able to hold; must be a non-negative integer.
         * @return a new float array with the specified size.
         * @throws IndexOutOfBoundsException if the specified size is negative.
         */
        @Override
        protected @NonNull Object createArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException {
            enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
            return new float[size];
        }

        /**
         * Retrieves the size of the specified array.
         * The array must be a non-null float array.
         *
         * @param array the non-null array whose size is to be determined, must be of type {@code float[]}.
         * @return the size of the array (number of elements it contains).
         * @throws NullPointerException if the input array is {@code null}.
         */
        @Override
        protected @Range(from = 0, to = Integer.MAX_VALUE) int getArraySize(final @NonNull Object array) throws NullPointerException {
            Objects.requireNonNull(array, "Array cannot be null.");
            return ((float[]) array).length;
        }

        /**
         * Converts the current set of blocks in the float array into a single aggregated array.
         * This method combines the underlying blocks of the dynamic float array into a
         * contiguous array representation.
         *
         * @return a non-null float array containing the combined data from all blocks.
         */
        @Override
        public float @NonNull [] asArray() {
            return (float[]) super.asArray();
        }
    }

    /**
     * A specialized dynamic array implementation to manage double values.
     * This class extends the {@code DynamicArray} to provide functionality
     * specific to handling an array of doubles with dynamic allocation.
     * <p>
     * The {@code Double} class is designed for scenarios where the size of the double array
     * is not fixed, and dynamic management is required. It supports operations
     * like adding double values, creating arrays of specified sizes, and
     * converting the data into a contiguous double array.
     */
    public static class Double extends DynamicArray {
        /**
         * Constructs a new {@code Double} instance with the specified granularity for memory allocation.
         * The granularity defines the size of memory blocks allocated dynamically for the array.
         *
         * @param granularity a value between 0 and 8 that determines memory allocation granularity.
         *                    Lower values represent less memory usage but higher allocation time, while
         *                    higher values represent more memory usage but lower allocation time.
         * @throws IndexOutOfBoundsException if the specified granularity is outside the range [0, 8].
         */
        public Double(final int granularity) throws IndexOutOfBoundsException {
            super(granularity);
        }

        /**
         * Constructs a new {@code Double} instance with default properties.
         * This no-argument constructor initializes the parent {@code DynamicArray}
         * with its default configuration by invoking its no-argument constructor.
         * <p>
         * This class is primarily used to manage and manipulate dynamic arrays of doubles with
         * features like adding elements dynamically, creating arrays, and converting the data
         * to a contiguous double array.
         */
        public Double() {
            super();
        }

        /**
         * Adds a single double value to the array.
         * If no available block exists, a new block is created to accommodate the value.
         *
         * @param value the double value to be added to the array.
         */
        public void addSingle(final double value) {
            final ArrayBlock block = getAvailableBlockOrCreate();
            ((double[]) block.array)[block.size++] = value;
        }

        /**
         * Creates a new array with the specified size.
         * The array created will have a capacity equal to the provided size.
         *
         * @param size the number of elements the array should be able to hold; must be a non-negative integer.
         * @return a new double array with the specified size.
         * @throws IndexOutOfBoundsException if the specified size is negative.
         */
        @Override
        protected @NonNull Object createArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws IndexOutOfBoundsException {
            enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
            return new double[size];
        }

        /**
         * Retrieves the size of the specified array.
         * The array must be a non-null double array.
         *
         * @param array the non-null array whose size is to be determined, must be of type {@code double[]}.
         * @return the size of the array (number of elements it contains).
         * @throws NullPointerException if the input array is {@code null}.
         */
        @Override
        protected @Range(from = 0, to = Integer.MAX_VALUE) int getArraySize(final @NonNull Object array) throws NullPointerException {
            Objects.requireNonNull(array, "Array cannot be null.");
            return ((double[]) array).length;
        }

        /**
         * Converts the current set of blocks in the double array into a single-aggregated array.
         * This method combines the underlying blocks of the dynamic double array into a
         * contiguous array representation.
         *
         * @return a non-null double array containing the combined data from all blocks.
         */
        @Override
        public double @NonNull [] asArray() {
            return (double[]) super.asArray();
        }
    }
}
