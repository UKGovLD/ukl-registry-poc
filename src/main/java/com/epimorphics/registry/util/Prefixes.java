/******************************************************************
 * File:        Prefixes.java
 * Created by:  Dave Reynolds
 * Created on:  27 Jan 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *****************************************************************/

package com.epimorphics.registry.util;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.registry.core.Register;
import com.epimorphics.registry.core.Registry;
import com.epimorphics.registry.message.Message;
import com.epimorphics.registry.message.MessagingService;
import com.epimorphics.registry.message.ProcessIfChanges;
import com.epimorphics.registry.store.RegisterEntryInfo;
import com.epimorphics.registry.webapi.JsonContext;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileManager;

/**
 * Set of default prefixes used in registry descriptions and queries.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Prefixes {
    // Only used for bootstrapping
    public static final String PREFIXES_FILE = "prefixes.ttl";

    public static final String PREFIX_REGISTER = "/system/prefixes";
    
    protected static boolean listeningForChanges = false;

    static PrefixMapping prefixes;
    static PrefixMapping defaultPrefixes;
    static Map<String, Object> jsonldContext;
    
    static {
        defaultPrefixes = FileManager.get().loadModel(PREFIXES_FILE);
    }

    /**
     * Return the hardwired default prefixes used during boostrap 
     * and for internal query implementations
     */
    public static PrefixMapping getDefault() {
        return defaultPrefixes;
    }
    
    /**
     * Return the prefix mapping derived from the prefixes system register
     * use for external serializations
     */
    public static PrefixMapping get() {
        if (prefixes == null) {
            prefixes = loadPrefixes();
        }
        return prefixes;
    }
    
    /**
     * Return a default JSON-LD context declaring all the known prefixes
     */
    public static Map<String, Object> getJsonldContext() {
        if (jsonldContext == null) {
            jsonldContext = new HashMap<String, Object>();
            Map<String, String> map = get().getNsPrefixMap();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                jsonldContext.put(entry.getKey(), entry.getValue());
            }
        }
        return jsonldContext;
    }
    
    /**
     * Return the URI of the prefixes register
     */
    public static String getPrefixRegisterURI() {
        return  Registry.get().getBaseURI() + PREFIX_REGISTER;
    }
    
    /**
     * Return the URI of of the system json context
     */
    public static String getJsonContextURI() {
        return  Registry.get().getBaseURI() + JsonContext.JSON_CONTEXT_PATH;
    }
    
    /**
     * Flush the cache if system/prefixes register has been updated
     * TODO need notification mechanism to do this properly
     */
    public static void resetCache() {
        prefixes = null;
        jsonldContext = null;
    }
    
    private static PrefixMapping loadPrefixes() {
        if (!listeningForChanges) {
            MessagingService.Process reset = new MessagingService.Process(){
                @Override
                public void processMessage(Message message) {
                    resetCache();
                }
            };
            String target = Registry.get().getBaseURI() + PREFIX_REGISTER;
            Registry.get().getMessagingService().processMessages( new ProcessIfChanges(reset, target) );
            listeningForChanges = true;
        }
        
        Register prefixesRegister = new Register( ResourceFactory.createResource( getPrefixRegisterURI() ) );
        prefixesRegister.setStore( Registry.get().getStore() );
        PrefixMapping pm = ModelFactory.createDefaultModel();
        for (RegisterEntryInfo info : prefixesRegister.getMembers()) {
            pm.setNsPrefix(info.getNotation(), info.getEntityURI());
        }
        return pm;
   }

}
