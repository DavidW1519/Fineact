/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.group.data;

import java.io.Serializable;
import java.util.Objects;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

public class GroupRoleData implements Serializable {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final CodeValueData role;
    @SuppressWarnings("unused")
    private final Long clientId;
    @SuppressWarnings("unused")
    private final String clientName;

    public static final GroupRoleData template() {
        return new GroupRoleData(null, null, null, null);
    }

    public GroupRoleData(final Long id, final CodeValueData role, final Long clientId, final String clientName) {
        this.id = id;
        this.role = role;
        this.clientId = clientId;
        this.clientName = clientName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof GroupRoleData)) { return false; }
        GroupRoleData that = (GroupRoleData) o;
        return Objects.equals(id, that.id) && Objects.equals(role, that.role) && Objects.equals(clientId, that.clientId)
                && Objects.equals(clientName, that.clientName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, clientId, clientName);
    }
}
