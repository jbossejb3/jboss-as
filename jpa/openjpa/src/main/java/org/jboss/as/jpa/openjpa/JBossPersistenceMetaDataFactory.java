/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.jpa.openjpa;

import org.jboss.as.jpa.spi.PersistenceUnitMetadata;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;

import javax.persistence.Entity;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.persistence.PersistenceMetaDataFactory;

/**
 * OpenJPA MetaDataFactory that uses the annotation index provided by PersistenceUnitMetadata
 * to search entity classes from the persistence unit.
 *
 * @author Antti Laisi
 */
public class JBossPersistenceMetaDataFactory extends PersistenceMetaDataFactory {

    private static ThreadLocal<PersistenceUnitMetadata> persistenceUnitMetadata = new ThreadLocal<PersistenceUnitMetadata>();

    @Override
    protected Set<String> parsePersistentTypeNames(ClassLoader loader) {
        PersistenceUnitMetadata pu = persistenceUnitMetadata.get();
        if (pu == null) {
            return Collections.emptySet();
        }

        return findPersistenceTypeNames(pu);
    }

    static void setThreadLocalPersistenceUnitMetadata(PersistenceUnitMetadata pu) {
        persistenceUnitMetadata.set(pu);
    }

    static void clearThreadLocalPersistenceUnitMetadata() {
        persistenceUnitMetadata.remove();
    }

    private Set<String> findPersistenceTypeNames(PersistenceUnitMetadata pu) {
        Set<String> persistenceTypeNames = new HashSet<String>();

        for (Map.Entry<URL, Index> entry : pu.getAnnotationIndex().entrySet()) {
            List<AnnotationInstance> instances = entry.getValue().getAnnotations(DotName.createSimple(Entity.class.getName()));
            for (AnnotationInstance instance : instances) {
                AnnotationTarget target = instance.target();
                if (target instanceof ClassInfo) {
                    ClassInfo classInfo = (ClassInfo) target;
                    persistenceTypeNames.add(classInfo.name().toString());
                }
            }
        }
        return persistenceTypeNames;
    }

}
