part of 'main.dart';

class DropdownButtonMaskList extends StatefulWidget {
  const DropdownButtonMaskList({super.key});

  @override
  State<DropdownButtonMaskList> createState() => _DropdownButtonMaskListState();
}

class _DropdownButtonMaskListState extends State<DropdownButtonMaskList> {
  String dropdownValue = '';

  autoSelect(dropMaskList) {
    if (dropdownValue.isEmpty) {
      setMaskMode(dropMaskList.first);
      print(dropMaskList.first);
      return dropMaskList.first;
    } else {
      return dropdownValue;
    }
  }

  setMaskMode(v) {
    List a = v.split(':');
    mode = a[0];
    print("Selected mode: $mode");
    mask = a[1];
    print("Selected mask: $mask");
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          FutureBuilder<List<String>?>(
              future: _getDocumentsTypes(),
              builder: (BuildContext context, AsyncSnapshot<List<String>?> snapshot) {
                if (snapshot.connectionState == ConnectionState.done) {
                  if (snapshot.hasError) {
                    return Text('${snapshot.error}');
                  } else {

                    var dropMaskList = snapshot.data ?? ['default:*8'];

                    return DropdownButton<String>(
                      // https://stackoverflow.com/a/74601580/2497351
                      value: autoSelect(dropMaskList),
                      icon: const Icon(Icons.arrow_downward),
                      itemHeight: null,
                      isExpanded: true,
                      elevation: 16,
                      //style: const TextStyle(color: Colors.black26),
                      underline: Container(
                        height: 2,
                        color: const Color.fromRGBO(0, 0, 0, 0.259),
                      ),
                      onChanged: (String? value) {
                        // This is called when the user selects an item.
                        setState(() {
                          dropdownValue = value!;
                          setMaskMode(value);
                        });
                      },
                      selectedItemBuilder: (BuildContext context) {
                        return dropMaskList.map<Widget>((String item) {
                          return Text(item, overflow: TextOverflow.ellipsis);
                        }).toList();
                      },
                      items: dropMaskList
                          .map<DropdownMenuItem<String>>((String value) {
                        return DropdownMenuItem<String>(
                          value: value,
                          child: Text(value, maxLines: 2),
                        );
                      }).toList(),
                    );
                  }
                } else {
                  return const Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Padding(
                        padding: EdgeInsets.fromLTRB(0, 8, 12, 8),
                        child: SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(),
                        ),
                      ),
                      Text('Awaiting docs...'),
                    ],
                  );
                }
              }),
        ],
      ),
    );
  }
}


Future<List<String>> _getDocumentsTypes() async {
  var types = <String>[];

  try {
    // Parse json
    String jData = await OCRSTUDIOSDK.engineDescription() ?? '';

    final parsedJson = jsonDecode(jData);
    final targets = EngineDescription.fromJson(parsedJson);

    for (var v in targets.targetGroups) {
      types.add('${v.targetGroupType}:${v.targetMasks[0]}');
    }

    print("targets: ");
    print(targets);
  } on PlatformException {}

  return types;
}
