package com.quicktrim.ai.domain.mapper

import com.quicktrim.ai.data.local.entity.ProjectDbEntity
import com.quicktrim.ai.data.local.entity.SegmentDbEntity
import com.quicktrim.ai.data.local.entity.SegmentedJsonFormatDbEntity
import com.quicktrim.ai.data.local.entity.WordDbEntity
import com.quicktrim.ai.data.remote.model.SegmentRemoteResponse
import com.quicktrim.ai.data.remote.model.SegmentedJsonFormatRemoteResponse
import com.quicktrim.ai.data.remote.model.WordRemoteResponse
import com.quicktrim.ai.domain.model.Project
import com.quicktrim.ai.domain.model.Segment
import com.quicktrim.ai.domain.model.SegmentedJsonFormat
import com.quicktrim.ai.domain.model.Word
import java.util.UUID

fun ProjectDbEntity.toProject(): Project {
    return Project(
        id = this.id,
        projectName = this.projectName,
        originalMediaUrl = this.originalMediaUrl,
        exportedMediaUrl = this.exportedMediaUrl,
        segmentedJsonFormat = this.segmentedJsonFormatDbEntity?.toSegmentedJsonFormat(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun SegmentedJsonFormatDbEntity.toSegmentedJsonFormat(): SegmentedJsonFormat {
    return SegmentedJsonFormat(
        segments = this.segments.map { it.toSegment() }
    )
}

fun SegmentDbEntity.toSegment(): Segment {
    return Segment(
        text = this.text,
        words = this.words.map { it.toWord() },
        id = this.id
    )
}

fun WordDbEntity.toWord(): Word {
    return Word(
        id = this.id,
        end = this.end,
        logprob = this.logprob,
        speakerId = this.speakerId,
        start = this.start,
        text = this.text,
        type = this.type,
        isRemoved = this.isRemoved
    )
}

fun Project.toProjectDbEntity(): ProjectDbEntity {
    return ProjectDbEntity(
        projectName = this.projectName,
        originalMediaUrl = this.originalMediaUrl,
        segmentedJsonFormatDbEntity = this.segmentedJsonFormat?.toSegmentJsonFormatDbEntity(),
        exportedMediaUrl = this.exportedMediaUrl,
        extractedAudioUrl = this.extractedAudioUrl,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun SegmentedJsonFormat.toSegmentJsonFormatDbEntity(): SegmentedJsonFormatDbEntity {
    return SegmentedJsonFormatDbEntity(
        segments = this.segments.map { it.toSegmentDbEntity() }
    )
}

fun Segment.toSegmentDbEntity(): SegmentDbEntity {
    return SegmentDbEntity(
        id = this.id,
        text = this.text,
        words = this.words.map { it.toWordDbEntity() },
        isRemoved = this.isRemoved
    )
}

fun Word.toWordDbEntity(): WordDbEntity {
    return WordDbEntity(
        id = this.id,
        end = this.end,
        logprob = this.logprob,
        speakerId = this.speakerId,
        start = this.start,
        text = this.text,
        type = this.type,
        isRemoved = this.isRemoved
    )
}

fun SegmentedJsonFormatRemoteResponse.toSegmentedJsonFormat() : SegmentedJsonFormat {
    return SegmentedJsonFormat(
        segments = this.segments.map { it.toSegment() }
    )
}

fun SegmentRemoteResponse.toSegment() : Segment {
    return Segment(
        id = UUID.randomUUID().toString(),
        text = this.text,
        words = this.words.map { it.toWordDbEntity() },
        isRemoved = false
    )
}

fun WordRemoteResponse.toWordDbEntity(): Word {
    return Word(
        id = UUID.randomUUID().toString(),
        end = this.end,
        logprob = this.logprob,
        speakerId = this.speakerId,
        start = this.start,
        text = this.text,
        type = this.type,
    )
}