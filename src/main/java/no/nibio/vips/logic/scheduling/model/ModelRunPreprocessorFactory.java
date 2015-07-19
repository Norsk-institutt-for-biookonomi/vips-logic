/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.scheduling.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.Reflections;

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
            return impl != null ? impl.newInstance() : null;
        } 
        catch (InstantiationException | IllegalAccessException ex) 
        {
            Logger.getLogger(ModelRunPreprocessorFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private static Map<String,Class<? extends ModelRunPreprocessor>> getModelRunPreprocessors()
    {
        if(modelRunPreprocessors == null)
        {
            modelRunPreprocessors = new HashMap<>();
            Reflections reflections;
            Set<Class<? extends ModelRunPreprocessor>> subTypes = new HashSet();
            reflections = new Reflections("no.nibio.vips.logic");
            subTypes.addAll(reflections.getSubTypesOf(ModelRunPreprocessor.class));
            for(Class<? extends ModelRunPreprocessor> subType : subTypes)
            {
                try 
                {
                    modelRunPreprocessors.put(subType.newInstance().getModelId(), subType);
                } 
                catch ( InstantiationException | IllegalAccessException ex) 
                {
                    Logger.getLogger(ModelRunPreprocessorFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return modelRunPreprocessors;
    }
}
