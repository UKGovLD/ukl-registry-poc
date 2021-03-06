/******************************************************************
 * File:        CommandRead.java
 * Created by:  Dave Reynolds
 * Created on:  22 Jan 2013
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

package com.epimorphics.registry.commands;

import javax.ws.rs.core.Response;

import com.epimorphics.registry.core.Command;
import com.epimorphics.registry.core.Register;
import com.epimorphics.registry.core.RegisterItem;
import com.epimorphics.registry.core.Status;
import com.epimorphics.registry.store.RegisterEntryInfo;
import com.sun.jersey.api.NotFoundException;


public class CommandDelete extends Command {

    @Override
    public Response doExecute() {
        store.lock(target);
        try {
            RegisterItem ri = store.getItem(itemURI(), false);
            if (ri != null) {
                if (ri.isRegister()) {
                    Register register = ri.getAsRegister(store);
                    for (RegisterEntryInfo entry : store.listMembers(register)) {
                        RegisterItem i = store.getItem(entry.getItemURI(), false);
                        doDelete(i);
                    }
                    doDelete(ri);
                } else {
                    doDelete(ri);
                }
                return Response.noContent().build();
            } else {
                throw new NotFoundException();
            }
        } finally {
            store.unlock(target);
        }
    }

    private void doDelete(RegisterItem ri) {
        ri.setStatus(Status.Invalid);
        store.update(ri, false);
    }
}
