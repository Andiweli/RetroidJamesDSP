umount /vendor/etc/audio/audio_policy_configuration.xml 2>/dev/null
umount /vendor/etc/audio_effects.xml 2>/dev/null
umount /vendor/etc/acdbdata/MTP 2>/dev/null
umount /vendor/etc/audio_policy_volumes.xml 2>/dev/null
umount /vendor/etc/default_volume_tables.xml 2>/dev/null
umount /vendor/etc/mixer_paths_qrd.xml 2>/dev/null
for m in $(mount | grep jdsp4rp5_tmpfs | awk -F' on ' '{print $2}' | awk -F' type ' '{print $1}') ; do umount -l "$m" 2>/dev/null ; done
for m in $(mount | grep /vendor/lib/soundfx | awk -F' on ' '{print $2}' | awk -F' type ' '{print $1}') ; do umount -l "$m" 2>/dev/null ; done
killall -q audioserver
killall -q mediaserver
