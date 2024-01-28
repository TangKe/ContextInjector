# ContextInjector

Inject application context reference to any where automatically. You can also inject the reference as you wish manually.

### Usage

1. add following line in your project build.gradle file

   ```groovy
   dependencies {
       annotationProcessor "com.github.TangKe:ContextInjectorCompiler:latest.version"
       annotationProcessor "com.google.auto.service:auto-service:1.0-rc7"
       implementation "com.github.TangKe:ContextInjector:latest.version"
   }
   ```

2. add annotation `@InjectContext` to ***fields*** or ***method*** where you want, then invoke the `ContextInjector.inject(Object)` method (method must have **only one parameter** with `Context` type, **if your injected elements were modified by `static`, the library will assign value or invoke method automatically when the Application been created, you don't need to invoke `ContextInjector.inejct(Object)` method**)

3. just enjoy

   <u>If your application contains more than one process, you should invoke `ContextInject.installMultiProcess(Application application)` in your `Application.onCreate()` method</u>

