package com.floreysoft.jmte;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { InterpretedEngineTest.class, 
//	CompiledEngineTest.class,
		MiniParserTest.class })
public class AllTests {

}