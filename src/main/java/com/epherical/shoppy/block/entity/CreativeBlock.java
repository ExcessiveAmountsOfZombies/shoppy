package com.epherical.shoppy.block.entity;

import java.util.UUID;

public interface CreativeBlock {

    void setOwner(UUID owner);

    UUID getOwner();
}
