# Used to find the diff of .launch file on merge conflicts

import re

launchfile = open("../openhab-distro/launch/openHAB_Runtime.launch").readlines()

for key in ["selected_target_plugins", "selected_workspace_plugins"]:
    print("Key:", key)
    matches = [re.search("value=\"([^\"]+)\"", l).group(1) for l in launchfile if key in l]
    entries = [set(entry_list.split(",")) for entry_list in matches]
    if len(entries) != 2:
        print("Invalid number of tags", len(entries))
    else:
        print("First:", entries[1] - entries[0])
        print("Second:", entries[0] - entries[1])



