part of 'main.dart';

class EngineScreenExample extends StatefulWidget {
  final Items result;

  const EngineScreenExample({super.key, required this.result});

  @override
  State<EngineScreenExample> createState() => _ScreenState();
}

class _ScreenState extends State<EngineScreenExample> {
  @override
  Widget build(BuildContext context) {

    Map fields = widget.result.string;
    Map images = widget.result.image;

    attrList(attr) {
      var children = <Widget>[];
      attr.forEach((k, v) {
        children.add(Container(
            decoration: BoxDecoration(
              color: Colors.black12,
              borderRadius: BorderRadius.circular(6),
            ),
            padding: const EdgeInsets.fromLTRB(4, 2, 4, 3),
            margin: const EdgeInsets.fromLTRB(0, 0, 0, 3),
            child: Text(' $k : $v ')));
      });

      return Column(
          crossAxisAlignment: CrossAxisAlignment.start, children: children);
    }

    fieldsW(elements, type) {
      return ListView.separated(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        //padding: const EdgeInsets.all(0),
        // sum all items for iteration in one stream
        itemCount: elements.length,
        itemBuilder: (BuildContext context, int index) {
          // render data fields
          var key = elements.keys.elementAt(index);

          if (type == 'fields') {
            return ListTile(
              minLeadingWidth: 16,
              visualDensity: VisualDensity.compact,
              minVerticalPadding: 0.0,
              leading: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    elements[key]['accepted']
                        ? const Icon(Icons.lens, color: Colors.green, size: 12)
                        : const Icon(Icons.lens, color: Colors.red, size: 12),
                  ]),
              title: Text(elements[key]['value']),
              subtitle: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  //mainAxisAlignment: MainAxisAlignment.start,
                  children: [
                    Padding(
                        padding: const EdgeInsets.fromLTRB(0, 2, 0, 5),
                        child: Text(key)),
                    if (elements[key]['attributes'] != null)
                      attrList(elements[key]['attributes'])
                  ]),
            );
          } //attrList(docData[key]['attr']));

          if (type == 'images') {
            return ListTile(
                minLeadingWidth: 16,
                visualDensity: VisualDensity.compact,
                //minVerticalPadding: 0.0,
                title: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Image.memory(base64.decode(elements[key]['image_base64']),
                          height: 150.0, fit: BoxFit.contain)
                      // Image.file(File(docImages[key]['value']), height: 150.0, fit: BoxFit.contain)
                    ]),
                subtitle: Text(elements[key]['name']));
          }

          return const Text("");
        },
        separatorBuilder: (BuildContext context, int index) =>
            const Divider(height: 8),
      );
    }

    textW(title) {
      return Column(
        children: [
          SizedBox(
            width: double.infinity,
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 20, 0, 10),
              child: Text(
                title,
                textAlign: TextAlign.left,
                style: const TextStyle(
                  fontSize: 22.0,
                ),
              ),
            ),
          ),
          const Divider(height: 8, thickness: 2),
        ],
      );
    }

    return Expanded(
      child: SingleChildScrollView(
        child: Column(
          children: [
            textW('Data fields'),
            fieldsW(fields, 'fields'),
            textW('Image fields'),
            fieldsW(images, 'images'),
          ],
        ),
      ),
    );
  }
}

