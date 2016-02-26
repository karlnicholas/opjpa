package test;

import java.util.ArrayList;
import java.util.List;

public class Factory {
	
	interface I {}
	
	List<I> iCache = new ArrayList<I>();

	class ClassA {}
	class ClassB {}
	
	abstract class Class1 extends ClassA implements I { abstract public void Do1Thing(); }
	abstract class Class2 extends ClassB implements I { abstract public void Do2Thing(); }
	
	class ImplTypeA extends Class1{ public void Do1Thing(){} }
	class ImplTypeB extends Class2{ public void Do2Thing(){} }

	I getOrCreateTypeA() { 
		for( I cls: iCache ) {
			if( cls instanceof Class1 ) return cls;
		}
		ImplTypeA cls = new ImplTypeA();
		iCache.add(cls);
		return cls;
	}
	I getOrCreateTypeB() { 
		for( I cls: iCache ) {
			if( cls instanceof Class2 ) return cls;
		}
		ImplTypeB cls = new ImplTypeB();
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
