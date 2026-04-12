package com.angelmirror.tracking

interface FacePoseAdapter<RawFace> {
    fun toFacePose(rawFace: RawFace): FacePose
}
