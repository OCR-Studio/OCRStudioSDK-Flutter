// Class generated with https://app.quicktype.io/?l=dart# from json response
import 'dart:convert';

class EngineResult {
    final bool isAllTargetsFinal;
    final List<TargetItem> targets;

    EngineResult({
      required this.isAllTargetsFinal,
      required this.targets,
    });

    factory EngineResult.fromJson(Map<String, dynamic> json) {
      final List<TargetItem> parsedTargets = [];
      if (json["targets"] != null) {
        final List rawList = json["targets"] as List;

        for (final rawItem in rawList) {
          if (rawItem is Map<String, dynamic>) {
            parsedTargets.add(TargetItem.fromJson(rawItem));
          }
        }
      }

      return EngineResult(
        isAllTargetsFinal: json["isAllTargetsFinal"] ?? false,
        targets: parsedTargets,
      );
    }

}

class TargetItem {
  final Description? description;
  final bool isFinal;
  final Items items;

  TargetItem({
    this.description,
    required this.isFinal,
    required this.items,
  });

  factory TargetItem.fromJson(Map<String, dynamic> json) {
    return TargetItem(
      isFinal: json["isFinal"] ?? false,
      description: json["description"] != null
          ? Description.fromJson(json["description"])
          : null,
      items: json["items"] != null
          ? Items.fromJson(json["items"])
          : Items(image: {}, string: {}, template: {}),
    );
  }
}

class Description {
    final String targetType;
    final String specificType;
    final int specificTypeHasRfid;
    final String specificTypeDisplayName;
    final List<String> itemTypes;
    final Attributes attributes;

    Description({
        required this.targetType,
        required this.specificType,
        required this.specificTypeHasRfid,
        required this.specificTypeDisplayName,
        required this.itemTypes,
        required this.attributes,
    });

    factory Description.fromJson(Map<String, dynamic> json) => Description(
        targetType: json["target_type"],
        specificType: json["specific_type"],
        specificTypeHasRfid: json["specific_type_has_rfid"],
        specificTypeDisplayName: json["specific_type_display_name"],
        itemTypes: List<String>.from(json["item_types"].map((x) => x)),
        attributes: Attributes.fromJson(json["attributes"]),
    );

}

class Attributes {
    Attributes();
    factory Attributes.fromJson(Map<String, dynamic> json) => Attributes();

}

class Items {
    final Map<String, dynamic> image;
    final Map<String, dynamic> string;
    final Map<String, dynamic> template;

    Items({
        required this.image,
        required this.string,
        required this.template,
    });

    factory Items.fromJson(Map<String, dynamic> json) => Items(
        image: json["image"],
        string: json["string"],
        template: json["template"],
    );
}
