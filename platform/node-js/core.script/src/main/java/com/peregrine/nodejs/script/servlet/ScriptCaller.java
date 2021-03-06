package com.peregrine.nodejs.script.servlet;

/**
 * Servlet Paths for the Node JS handling
 *
 * Created by Andreas Schaefer on 4/10/17.
 */
public interface ScriptCaller {
    public static final String EXECUTE_SCRIPT_WITH_NODE_JS = "/perapi/nodejs/execute/node";
    public static final String EXECUTE_SCRIPT_WITH_J2V8 = "/perapi/nodejs/execute/j2v8";
}
