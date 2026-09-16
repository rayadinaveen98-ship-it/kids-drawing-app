package com.navin.kidsdrawing.lesson.content

private val CATEGORY_LABELS = mapOf(
    "animals" to "Animals",
    "characters" to "People & Characters",
    "design" to "Design & Invent",
    "everyday" to "Everyday Things",
    "food" to "Food",
    "foundations" to "Drawing Basics",
    "imagination" to "Fantasy & Imagination",
    "nature" to "Nature",
    "portrait" to "Portraits",
    "scenes" to "Stories & Scenes",
    "space" to "Space & Science",
    "vehicles" to "Vehicles & Machines",
)

private val SKILL_LABELS = mapOf(
    "line.control" to "Line Control",
    "curves" to "Curves",
    "shape.construction" to "Shape Construction",
    "shape_construction" to "Shape Construction",
    "placement" to "Placement",
    "placement.symmetry" to "Symmetry",
    "proportion" to "Proportion",
    "proportion.basic" to "Basic Proportion",
    "overlap.basic" to "Overlap & Depth",
    "pattern" to "Texture & Pattern",
    "expression.face" to "Facial Expression",
    "pose.simple" to "Pose & Gesture",
    "perspective.one_point" to "One-Point Perspective",
    "composition.balance" to "Composition",
    "storytelling.character" to "Visual Storytelling",
)

private val JOURNEY_LABELS = mapOf(
    "journey.animal_artist" to "Animal Artist",
    "journey.character_creator" to "Character Creator",
    "journey.first_shapes_to_pictures" to "First Shapes to Pictures",
    "journey.space_artist" to "Space Artist",
)

enum class CatalogTaxonomyKind {
    CATEGORY,
    SKILL,
    JOURNEY,
    COLLECTION,
    CONTENT_FAMILY,
}

data class CatalogTaxonomyDefinition(
    val kind: CatalogTaxonomyKind,
    val id: String,
    val displayLabel: String,
)

data class CatalogTaxonomyRegistryDiagnostic(
    val kind: CatalogTaxonomyKind,
    val id: String,
    val message: String,
)

/**
 * Stable discovery vocabulary owned outside lesson packages.
 *
 * Entries carry IDs only. The registry owns whether an ID is accepted and how it is displayed.
 * Unknown IDs fail index validation instead of being silently invented or normalized at runtime.
 */
class CatalogTaxonomyRegistry(definitions: List<CatalogTaxonomyDefinition>) {
    private val definitions = definitions.toList()
    private val byKind = this.definitions.groupBy(CatalogTaxonomyDefinition::kind)
        .mapValues { (_, values) -> values.associateBy(CatalogTaxonomyDefinition::id) }

    val diagnostics: List<CatalogTaxonomyRegistryDiagnostic> = definitions
        .groupBy { it.kind to it.id }
        .filterValues { it.size > 1 }
        .keys
        .sortedWith(compareBy({ it.first.name }, { it.second }))
        .map { (kind, id) ->
            CatalogTaxonomyRegistryDiagnostic(
                kind = kind,
                id = id,
                message = "Duplicate ${kind.name.lowercase()} taxonomy ID '$id'.",
            )
        }

    fun contains(kind: CatalogTaxonomyKind, id: String): Boolean = byKind[kind]?.containsKey(id) == true

    fun definition(kind: CatalogTaxonomyKind, id: String): CatalogTaxonomyDefinition? = byKind[kind]?.get(id)

    fun definitions(kind: CatalogTaxonomyKind): List<CatalogTaxonomyDefinition> =
        byKind[kind].orEmpty().values.sortedBy(CatalogTaxonomyDefinition::id)

    companion object {
        fun fromIds(
            categories: Collection<String> = emptyList(),
            skills: Collection<String> = emptyList(),
            journeys: Collection<String> = emptyList(),
            collections: Collection<String> = emptyList(),
            contentFamilies: Collection<String> = emptyList(),
        ): CatalogTaxonomyRegistry = CatalogTaxonomyRegistry(
            buildList {
                addAll(categories.map { definition(CatalogTaxonomyKind.CATEGORY, it) })
                addAll(skills.map { definition(CatalogTaxonomyKind.SKILL, it) })
                addAll(journeys.map { definition(CatalogTaxonomyKind.JOURNEY, it) })
                addAll(collections.map { definition(CatalogTaxonomyKind.COLLECTION, it) })
                addAll(contentFamilies.map { definition(CatalogTaxonomyKind.CONTENT_FAMILY, it) })
            },
        )

        private fun definition(kind: CatalogTaxonomyKind, id: String) = CatalogTaxonomyDefinition(
            kind = kind,
            id = id,
            displayLabel = taxonomyDisplayLabel(kind, id),
        )
    }
}

/** Frozen V2.2 vocabulary inventoried from the 24 accepted release lessons. */
object CatalogTaxonomyV2 {
    val registry: CatalogTaxonomyRegistry = CatalogTaxonomyRegistry.fromIds(
        categories = setOf(
            "animals",
            "animals.birds",
            "animals.ocean",
            "animals.wild",
            "characters",
            "characters.cartoon",
            "design",
            "everyday",
            "food",
            "foundations",
            "foundations.lines",
            "foundations.shapes",
            "imagination",
            "nature",
            "nature.flowers",
            "nature.landscapes",
            "nature.plants",
            "nature.trees",
            "nature.weather",
            "objects",
            "pets",
            "portrait",
            "scenes",
            "sky",
            "space",
            "space.aliens",
            "space.planets",
            "space.rockets",
            "travel",
            "vehicles",
            "vehicles.land",
            "vehicles.water",
            "wildlife",
        ),
        skills = setOf(
            "anatomy.body_basic",
            "anatomy.face_basic",
            "balance",
            "character.design",
            "character.silhouette",
            "color.fill_control",
            "color.palette_choice",
            "composition.balance",
            "composition.centering",
            "construction",
            "contour.refinement",
            "contour.simple",
            "creative.choice",
            "creativity.imagination",
            "creativity.variation",
            "curves",
            "design.silhouette",
            "design.variation",
            "detail.layering",
            "detail.texture",
            "expression.face",
            "line.control",
            "line.curve",
            "line.loop",
            "line.straight",
            "line.zigzag",
            "observation",
            "overlap.basic",
            "pattern",
            "perspective.depth_scale",
            "perspective.one_point",
            "placement",
            "placement.relative",
            "placement.spacing",
            "placement.symmetry",
            "pose.simple",
            "proportion",
            "proportion.basic",
            "scale.relative",
            "scene.foreground_background",
            "shape.circle",
            "shape.combine",
            "shape.construction",
            "shape.ellipse",
            "shape.organic",
            "shape.oval",
            "shape.rectangle",
            "shape.square",
            "shape.triangle",
            "shape_construction",
            "simple_details",
            "storytelling.character",
        ),
        journeys = setOf(
            "journey.animal_artist",
            "journey.character_creator",
            "journey.first_shapes_to_pictures",
            "journey.space_artist",
        ),
        collections = emptySet(),
        contentFamilies = emptySet(),
    )
}

private fun taxonomyDisplayLabel(kind: CatalogTaxonomyKind, id: String): String {
    val overrides = when (kind) {
        CatalogTaxonomyKind.CATEGORY -> CATEGORY_LABELS
        CatalogTaxonomyKind.SKILL -> SKILL_LABELS
        CatalogTaxonomyKind.JOURNEY -> JOURNEY_LABELS
        CatalogTaxonomyKind.COLLECTION,
        CatalogTaxonomyKind.CONTENT_FAMILY,
        -> emptyMap()
    }
    return overrides[id] ?: id
        .substringAfterLast('.')
        .split('-', '_')
        .filter(String::isNotBlank)
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }
}
