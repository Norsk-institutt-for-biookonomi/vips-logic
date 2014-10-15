/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.scheduling.model;

/**
 * @copyright 2013 {@link http://www.bioforsk.no Bioforsk}
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class PreprocessorException extends Exception {

    /**
     * Creates a new instance of <code>PreprocessorException</code> without
     * detail message.
     */
    public PreprocessorException() {
    }

    /**
     * Constructs an instance of <code>PreprocessorException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public PreprocessorException(String msg) {
        super(msg);
    }
}
