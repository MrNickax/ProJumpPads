package com.nickax.jumppads.api;

import com.nickax.jumppads.pad.JumpPad;
import com.nickax.jumppads.util.Variable;

import java.util.List;

/**
 * API for interacting with JumpPads
 */
public class JumpPadsAPI {

    /**
     * Gets a list of all registered jump pads
     *
     * @return List of all jump pads
     */
    public static List<JumpPad> getJumpPads() {
        return Variable.jumpPads;
    }

    /**
     * Gets a jump pad by its name
     *
     * @param name Name of the jump pad to find
     * @return The jump pad with a matching name, or null if not found
     */
    public static JumpPad getByName(String name) {
        if (name == null || name.isEmpty()) return null;
        return Variable.jumpPads.stream().filter(jumpPad -> jumpPad.getName() != null && jumpPad.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}