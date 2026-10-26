/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.scheduling.model;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;

/**
 * Returns the appropriate factory for the given model
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ModelRunPreprocessorFactory {
    
    private static Map<String,Class<? extends ModelRunPreprocessor>> modelRunPreprocessors;

    public static ModelRunPreprocessor getModelRunPreprocessor(String modelId)
    {
        try 
        {
            Class<? extends ModelRunPreprocessor> impl = getModelRunPreprocessors().get(modelId);
            return impl != null ? impl.getDeclaredConstructor().newInstance() : null;
        } 
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) 
        {
            Logger.getLogger(ModelRunPreprocessorFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String,Class<? extends ModelRunPreprocessor>> getModelRunPreprocessors()
    {
        if(modelRunPreprocessors == null)
        {
            modelRunPreprocessors = new HashMap<>();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = ModelRunPreprocessorFactory.class.getClassLoader();
            }
            try {
                IndexView index = loadCompositeJandexIndex(classLoader);
                DotName preprocessorName = DotName.createSimple(ModelRunPreprocessor.class.getName());
                for (ClassInfo classInfo : index.getAllKnownSubclasses(preprocessorName)) {
                    try {
                        Class<? extends ModelRunPreprocessor> subType =
                                (Class<? extends ModelRunPreprocessor>) Class.forName(
                                        classInfo.name().toString(), true, classLoader);
                        modelRunPreprocessors.put(
                                subType.getDeclaredConstructor().newInstance().getModelId(), subType);
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                            | InvocationTargetException | NoSuchMethodException ex) {
                        Logger.getLogger(ModelRunPreprocessorFactory.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ModelRunPreprocessorFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return modelRunPreprocessors;
    }

    private static IndexView loadCompositeJandexIndex(ClassLoader classLoader) throws IOException {
        List<IndexView> indexes = new ArrayList<>();
        Enumeration<URL> resources = classLoader.getResources("META-INF/jandex.idx");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            try (InputStream indexStream = url.openStream()) {
                indexes.add(new IndexReader(indexStream).read());
            } catch (IOException ex) {
                Logger.getLogger(ModelRunPreprocessorFactory.class.getName()).log(Level.WARNING, "Skipping unreadable Jandex index " + url, ex);
            }
        }
        if (indexes.isEmpty()) {
            throw new IllegalStateException("Jandex index not found on classpath");
        }
        return indexes.size() == 1 ? indexes.get(0) : CompositeIndex.create(indexes);
    }
}
