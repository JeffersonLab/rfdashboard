package org.jlab.rfd.presentation.util;

import org.jlab.rfd.model.CryomoduleType;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a class designed to handle common mappings of cryomodule types.  The CED offers finer grained classes of
 * cryomodules that what users often want to see.  This provides a standard way to handle those mappings.
 * @author adamc
 */
public class CMTypeMapper {
    private final Map<String, String> stringMapper;
    private final Map<CryomoduleType, CryomoduleType> enumMapper;

    /**
     * Construct a new instance with the most common mapping.
     */
    public CMTypeMapper() {
        stringMapper = new HashMap<>();
        enumMapper = new HashMap<>();

        // These are one-off slight deviations from the major categories so users want them combined.
        put("F100", "C100");
        put("C50T", "C50");

        // Added here since the Booster type exists, but the convention is to use the QTR type for the new
        // (upgraded as of 2023) injector booster module.
        put("Booster", "QTR");
    }

    /**
     * Get the mapped type
     * @param type The original type
     * @return The mapped type or original if no mapping exists.
     */
    public String get(String type) {
        // Try to conversion as it will raise an IllegalArgumentException if not a real CMType
        CryomoduleType.valueOf(type);
        if (stringMapper.containsKey(type)) {
            return stringMapper.get(type);
        }
        return type;
    }

    /**
     * Get the mapped type
     * @param type The original type
     * @return The mapped type or original if no mapping exists.
     */
    public CryomoduleType get(CryomoduleType type) {
        if (enumMapper.containsKey(type)) {
            return enumMapper.get(type);
        }
        return type;
    }

    /**
     * Set or replace a mapping.  This takes a string, but both enum and string mappings are updated.
     * @param orig The original (i.e., from CED) type that is to be mapped
     * @param mapped The mapped target for the original value
     * @return The previous mapped value or null
     */
    public String put(String orig, String mapped) {
        // Do this first to trigger the error if it's not a valid type
        CryomoduleType enumOrig = CryomoduleType.valueOf(orig);
        CryomoduleType enumMapped = CryomoduleType.valueOf(mapped);
        enumMapper.put(enumOrig, enumMapped);
        return stringMapper.put(orig, mapped);
    }

    /**
     * Set or replace a mapping.  This takes an enum, but both enum and string mappings are updated.
     * @param orig The original (i.e., from CED) type that is to be mapped
     * @param mapped The mapped target for the original value
     * @return The previous mapped value or null
     */
    public CryomoduleType put(CryomoduleType orig, CryomoduleType mapped) {
        // Do this first to trigger the error if it's not a valid type
        String stringOrig = orig.toString();
        String stringMapped = mapped.toString();
        stringMapper.put(stringOrig, stringMapped);
        return enumMapper.put(orig, mapped);
    }
}
