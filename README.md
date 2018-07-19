# ContextInjector

Inject application context reference to any where automatically. You can also inject the reference as you wish manually.

### Usage

1. add following line in your root build.gradle file

   ```groovy
   buildscript {
       dependencies {
           classpath 'ke.tang:context-injector-plugin:1.0.1'
       }
   }
   ```

2. apply plugin in your module build.gradle file

   ```groovy
   apply plugin: "context-injector"
   ```

3. add annotation `InjectContext` to ***fields*** or ***method*** what you want(method must have one parameter with Context type, **if your injected elements were `static` modified, the library will assign value or invoke method automatically**)

4. enjoy


