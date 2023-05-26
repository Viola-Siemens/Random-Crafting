package com.hexagram2021.randomcrafting.util;

import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

public final class ListShuffler {
	private ListShuffler() {}

	private static final int SHUFFLE_THRESHOLD = 8;

	@SuppressWarnings("unchecked")
	public static <T> void shuffle(@NotNull List<T> list, @NotNull RandomSource rnd) {
		int size = list.size();
		if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
			for (int i = size; i > 1; i--) {
				swap(list, i - 1, rnd.nextInt(i));
			}
		} else {
			Object[] arr = list.toArray();

			for (int i = size; i > 1; i--) {
				swap(arr, i - 1, rnd.nextInt(i));
			}

			ListIterator<T> it = list.listIterator();
			for (Object e : arr) {
				it.next();
				it.set((T)e);
			}
		}
	}

	private static <T> void swap(List<T> list, int i, int j) {
		list.set(i, list.set(j, list.get(i)));
	}

	private static void swap(Object[] arr, int i, int j) {
		Object tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}
}
