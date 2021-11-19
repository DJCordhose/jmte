Release 1.0
===========

First complete public release

Release 2.0 rc1
===============

Second complete public release mainly fixing design flaws and providing better API. Template language slightly extended.

*Cleaner lexing allowing for better analysis of templates
- Lexer now only does the job of a scanner without using evaluation. Evaluation is done on token when needed. 

*New error reporting using properties mechanism
- We now have i18n error messages that can be formatted with any locale in a lazy style

*More helper functions
- You can now merge multiple lists into a single one allowing to iterate them in parallel using the engine

*Generally more graceful reactions to expressions that might seem inappropriate
- If and foreach now try to make sense of any type of object being passed in as an expression parameter
- Even a string expression tries to spit out any kind of object

*Lots of internal refactoring and bug fixing

*Introduced renderers

*Introduced compiled mode

*Introduced dynamic expressions and a configurable model adaptor

Release 2.0 rc2
===============

* Samples classes stripped from jars

* Added cached interpreted mode that features speedup close to compiled version (switched on as default)

Release 2.0 rc3
===============

* Cleaned caliper tests

* Fixed source release (missing libs and build files)

* Reintroduced Engine.format as a non-static high performance version

* Fixed infinite loop on certain parsing errors

* Suppressed error messages on stderr when executing tests

* Added a real world example/test

* Added comment token ${-- comment}

* Wrapped all exceptions in Util into RuntimeException

* All renderers and process listener now get the template context as an additional parameter

* Reflection code optimized for performance

Release 2.0 final
=================

no changes from 2.0 rc3

Release 2.1
===========

* Fixed i18n build problem

* Added annotation token ${@ ...}, e.g. for static type declarations

* Engine now thread safe on public methods

* Minor cleaning of API visibilities

* ProcessListener is now passed on a per transformation basis

Release 2.2
===========

* Performance optimizations

* Compiled classes are no longer stored globally and forever, but live as long as the engine - each engine has its own class loader that dies with it

* Now direct access to template is possible as an alternative to the Engine#transform call

* Used variables for interpreted templates are now buffered (have always been buffered for compiled templates)

* Introduced compiler interface "TemplateCompiler"

* Made compiler configurable in Engine allowing for very different compiler implementations even making use of annotations (e.g. for static type information)

* Template now is interface instead of abstract class

* Made all renderer resolvation methods public to be accessible from outside compilers (also added generic parameters)

* Introduced RendererRegistry interface implemented by Engine

* Reverted decision that all renderers get the template context as an additional parameter - now without context again

* Renamed test package "realLive" to "realLife"


Release 3.0
===========

* Engine now holds current version number in #VERSION copied from common.properties

* Engine now supports locale as parameter for transformation that gets passed into TemplateContext for processors and into renderers as an additional parameter - good for locale dependent rendering / processing - breaks public API, though

* NoLogErrorHandler introduced which does does not log, but only throws an exception

* Engine now supports check if variables (expressions) are present in a specific model using #variablesAvailable

* fixed Issue 10: Backslashes in Model data are (incorrectly) removed

* Added Issue 8, fixed Issue 9: add pom.xml to support Maven builds

* fixed Issue 11: Duplicate fields in InterpretedTemplate may cause NPE under some circumstances
  (note: it has impact only in case of some code refactoring)
  
* partially fixed Issue 12: Date Junit Tests fail on different Timezone and/or Locale than MEZ/Germany
  (workaround for CET (Czech) and MET (Germany) Time Zones, but probably fail elsewhere)

Release 3.1
===========

* New Feature: Iteration variable "_it" introduced in foreach loop, e.g. ${foreach list i}${_it}${end} is the same as ${foreach list i}${i}${end}

* New Feature: Introduced new encoder concept that optionally encodes all rendered strings

* Supplied first encoder for XML

* New Feature: Introduced RawRenderer interface that lets renderer be unaffected by configured encoder

* New Feature: Added one-based iteration variable "index_..."in foreach loop, e.g. ${foreach array item \n}${index_item}. ${item}${end}
  (proposed by http://code.google.com/u/bchoii/)

Release 3.1.1
=============

* Fixed Issue 22 (https://code.google.com/p/jmte/issues/detail?id=22): Dependency to ASM again optional

Feature Release 3.2.0
=====================

* Fixed Bug: String literals in comparisons can now contain spaces
* Now building for JDK 1.7
* Transformation of model value to iterable for for each now done in ModelAdaptor
* ModelAdapter can be configured to treat everything as a list when looping over it, prevents map to be iterated over as entries
* ModelAdapter now manages special iterator alias, DefaultModelAdapter provides "_it" for backward compatibility 
* ModelAdapter now has a (slow) fallback to access maps with keys that are not strings (iterating through all keys, turning them to strings and compare), can be deactivated

Feature Release 3.3.0
=====================

* Enhancement: Improved Maven Build to include sources and javadoc
* New Feature: Added Engine#getUsedVariableDescriptions to get more detailed descriptions of used variables
* New Feature: Renderer can now indicate that they can accept null values using a marker interface NullRenderer

Major Release 4.0.0
===================

Date: 05.03.2017
Tag: 4.0.0

* Breaking Change: Token can now contain annotations
* Breaking Change: ErrorHandler now takes newly introduced ErrorMessage enum  (instead of just a string)
* Deprecation: Cached and Compiled Templates are deprecated and are no longer actively maintained - will be removed completely in next major version
* Deprecation: Engine#getUsedVariables
* Breaking Change / Deprecation: Disabled enabledInterpretedTemplateCache and deprecated all uses in Engine
* New Feature: Arrays now accept indices and have a computed length property
* New Feature: Added journaling error handler (JournalingErrorHandler) and OutputAppender abstraction to embed error messages directly in output using ErrorReportingOutputAppender (see ErrorReportingTest)
* New Feature: When iterating over maps you can now choose between values, keys, and entries (map._values, map._keys, map.entries) 
* New Feature: Engine#getStaticErrors exposing ErrorEntry refactored from JournalingErrorHandler.Entry
* New Feature: Allow renderers to be applied before comparing in if statements (see BooleanIfRendererTest)
* New Feature: Added AbstractParameterRenderer to ease processing of parameters for Renderers
* New Feature: Slices of an Array can be accessed (https://github.com/DJCordhose/jmte/pull/7)

Feature Release 4.1.0
=====================
* New Feature: TemplateContext now provides access to underlying StringBuilder creating the output

Major Release 5.0.0
===================

Date: 07.11.2017
Tag: 5.0.0

* Breaking Change: Completely removed Abstraction over Template types, only interpreted survives
* Breaking Change: Removed all API deprecated in version 4
* Breaking Change: Switch to Java 8 language level
* Bug fix / Enhancement: if statement more robust (https://github.com/DJCordhose/jmte/issues/8)
* Refactoring / Cleanup: Removed all caliper tests and real life test
* Deprecation: Configurable expression syntax, will be fixed to ${...}
* Enhancement: transform using a *null* template will yield *null*

Major Release 6.0.0
===================

Date: 11.02.2020
Tag: 6.0.0

* Breaking Change: if can now compare a variable against another (https://github.com/DJCordhose/jmte/pull/12) 

Major Release 7.0.0
===================

Date: not yet published

* Breaking Change: brute force approach to make MiniParser thread safe (all methods are now synchronized)
* JMTE is is in low maintenance mode: critical bugs will be fixed, but no new features