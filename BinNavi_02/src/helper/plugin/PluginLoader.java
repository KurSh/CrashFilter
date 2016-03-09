package helper.plugin;

import com.google.security.zynamics.binnavi.API.plugins.IPluginServer;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PluginLoader
  implements IPluginServer<PluginInterface>
{
  public Collection<IPlugin<PluginInterface>> getPlugins()
  {
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(new CrashFilterPlugin());

    return localArrayList;
  }
  public static void main (String[] args )
  {
	  
  }
}