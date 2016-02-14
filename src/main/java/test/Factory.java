package test;

import java.util.ArrayList;
import java.util.List;

public class Factory {
	
	interface I {}
	
	List<I> iCache = new ArrayList<I>();

	abstract class ClassA {}
	abstract class ClassB {}
	
	class Class1 extends ClassA implements I {}
	class Class2 extends ClassB implements I {}

	I getOrCreateTypeA() { 
		for( I cls: iCache ) {
			if( cls instanceof ClassA ) return cls;
		}
		Class1 cls = new Class1();
		iCache.add(cls);
		return cls;
	}
	I getOrCreateTypeB() { 
		for( I cls: iCache ) {
			if( cls instanceof ClassB ) return cls;
		}
		Class2 cls = new Class2();
		iCache.add(cls);
		return cls;
	}
	
	I getOrCreate(Class<?> cls) {
		if ( ClassA.class.isAssignableFrom(cls)) {
			return getOrCreateTypeA();
		} else if ( ClassB.class.isAssignableFrom(cls)) {
			return getOrCreateTypeB();
		}
		return null;
	}

	void run() {
		I classI1 = getOrCreate(Class1.class);
		System.out.println(classI1);
		I classI2 = getOrCreate(Class2.class);
		System.out.println(classI2);
		I classI3 = getOrCreate(Class1.class);
		System.out.println(classI3);
		System.out.println(iCache);
	}
	
	public static void main(String... args) {
		new Factory().run();
	}

}
