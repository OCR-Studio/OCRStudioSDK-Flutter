// Class generated with https://app.quicktype.io/?l=dart# from json response


/* example

{
    "session_types": [
        "document_recognition",
        "video_recognition",
        "video_authentication"
    ],
    "target_groups": [
        {
            "target_group_type": "anyafrica",
            "targets": [
                "ago.drvlic.type1",
                "ago.id.type1",
                "zwe.id.type1",
                "zwe.passport.type1",
                "zwe.passport.type2"
            ],
            "target_masks": [
                "*"
            ]
        }
    ]
}

*/


class EngineDescription {
    final List<String> sessionTypes;
    final List<TargetGroup> targetGroups;

    EngineDescription({
        required this.sessionTypes,
        required this.targetGroups,
    });

    factory EngineDescription.fromJson(Map<String, dynamic> json) => EngineDescription(
        sessionTypes: List<String>.from(json["session_types"].map((x) => x)),
        targetGroups: List<TargetGroup>.from(json["target_groups"].map((x) => TargetGroup.fromJson(x))),
    );

    Map<String, dynamic> toJson() => {
        "session_types": List<dynamic>.from(sessionTypes.map((x) => x)),
        "target_groups": List<dynamic>.from(targetGroups.map((x) => x.toJson())),
    };
}

class TargetGroup {
    final String targetGroupType;
    final List<String> targets;
    final List<String> targetMasks;

    TargetGroup({
        required this.targetGroupType,
        required this.targets,
        required this.targetMasks,
    });

    factory TargetGroup.fromJson(Map<String, dynamic> json) => TargetGroup(
        targetGroupType: json["target_group_type"],
        targets: List<String>.from(json["targets"].map((x) => x)),
        targetMasks: List<String>.from(json["target_masks"].map((x) => x)),
    );

    Map<String, dynamic> toJson() => {
        "target_group_type": targetGroupType,
        "targets": List<String>.from(targets.map((x) => x)),
        "target_masks": List<String>.from(targetMasks.map((x) => x)),
    };
}
