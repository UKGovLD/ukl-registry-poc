/******************************************************************
 * File:        CommandRead.java
 * Created by:  Dave Reynolds
 * Created on:  22 Jan 2013
 *
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.registry.commands;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.registry.core.Command;
import com.epimorphics.registry.core.RegisterItem;
import com.epimorphics.registry.core.Registry;
import com.epimorphics.registry.vocab.RegistryVocab;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.sun.jersey.api.NotFoundException;


public class CommandStatusUpdate extends Command {
    public static final String STATUS_PARAM = "status";

    public CommandStatusUpdate(Operation operation, String target,
            MultivaluedMap<String, String> parameters, Registry registry) {
        super(operation, target, parameters, registry);
    }

    @Override
    public Response execute() {
        store.lock(target);
        RegisterItem ri = store.getItem(target, false);
        try {
            if (ri != null) {
                // TODO auth
                // TODO lifecyle checks
                // TODO handle verification for accepted
                // TODO handle deletion for Invalid
                String requestedStatus = parameters.getFirst(STATUS_PARAM);
                Resource status = ri.setStatus(requestedStatus);
                if (status == null) {
                    throw new WebApplicationException(Response.Status.FORBIDDEN);
                }
                if (status.equals(RegistryVocab.statusExperimental) || status.equals(RegistryVocab.statusStable)) {
                    RDFUtil.timestamp(ri.getRoot(), DCTerms.dateAccepted);
                }
                store.update(ri, false);
                return Response.noContent().build();
            } else {
                throw new NotFoundException();
            }
        } finally {
            store.unlock(target);
        }
    }

}
