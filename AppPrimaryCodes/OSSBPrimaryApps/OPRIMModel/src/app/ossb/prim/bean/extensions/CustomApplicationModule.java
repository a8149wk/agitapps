package app.ossb.prim.bean.extensions;

import oracle.jbo.ApplicationModule;

public interface CustomApplicationModule extends ApplicationModule{
  void callProcWithNoArgs();
}
