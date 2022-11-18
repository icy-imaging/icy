package icy.plugin.interface_;

import javax.script.ScriptEngineFactory;

/**
 * Plugin interface to provide scripting language factory (see {@link ScriptEngineFactory})
 * 
 * @author Stephane Dallongeville
 */
public interface PluginScriptFactory
{
    /**
     * @return the {@link ScriptEngineFactory}
     */
    public ScriptEngineFactory getScriptEngineFactory();
}
