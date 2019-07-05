package space.engine.baseobject;

import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

class ToStringDefaultEntries {
	
	//constant
	private static final BiFunction<ToStringHelper<?>, Collection, Object> ENTRY_LIST = (api, list) -> api.toString(list.toArray());
	private static final BiFunction<ToStringHelper<?>, Map, Object> ENTRY_MAP = (api, map) -> {
		ToStringHelperTable<?> mapper = api.createMapper(map.getClass().getName(), "->", true);
		int index = 0;
		for (Entry entry : ((Map<?, ?>) map).entrySet()) {
			mapper.put(new int[] {index, 0}, api.toString(entry.getKey()));
			mapper.put(new int[] {index, 1}, api.toString(entry.getValue()));
			index++;
		}
		return mapper;
	};
	
	//init
	static Void init0() {
		init();
		return null;
	}
	
	@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
	private static void init() {
		//List
		ToString.manualEntry(ArrayList.class, ENTRY_LIST);
		ToString.manualEntry(LinkedList.class, ENTRY_LIST);
		
		//map
		HashMap<Object, Object> hashMap = new HashMap<>();
		ToString.manualEntry(HashMap.class, ENTRY_MAP);
		ToString.manualEntry(hashMap.keySet().getClass(), ENTRY_LIST);
		ToString.manualEntry(hashMap.values().getClass(), ENTRY_LIST);
		
		EnumMap<SomeEnum, Object> enumMap = new EnumMap<>(SomeEnum.class);
		ToString.manualEntry(EnumMap.class, ENTRY_MAP);
		ToString.manualEntry(enumMap.keySet().getClass(), ENTRY_LIST);
		ToString.manualEntry(enumMap.values().getClass(), ENTRY_LIST);
	}
	
	private enum SomeEnum {
		
	}
}
