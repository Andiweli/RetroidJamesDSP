SDIR="$1"
TMPFS="$SDIR/support/jdsp4rp5_tmpfs"
SOUNDFX_DIR=/vendor/lib/soundfx

umount /vendor/etc/audio/audio_policy_configuration.xml 2>/dev/null
for m in $(mount | grep tmpfs | grep $(basename $TMPFS) | awk -F' on ' '{print $2}' | awk -F' type ' '{print $1}') ; do umount -l "$m" 2>/dev/null ; done
for m in $(mount | grep tmpfs | grep "$SOUNDFX_DIR" | awk -F' on ' '{print $2}' | awk -F' type ' '{print $1}') ; do umount -l "$m" 2>/dev/null ; done
umount /vendor/etc/audio_effects.xml 2>/dev/null
umount /vendor/etc/acdbdata/MTP 2>/dev/null
umount /vendor/etc/audio_policy_volumes.xml 2>/dev/null
umount /vendor/etc/default_volume_tables.xml 2>/dev/null
umount /vendor/etc/mixer_paths_qrd.xml 2>/dev/null

mount -o bind $SDIR/support/conf_files/audio_policy_configuration.xml /vendor/etc/audio/audio_policy_configuration.xml
chown root:root /vendor/etc/audio/audio_policy_configuration.xml
chmod 0644 /vendor/etc/audio/audio_policy_configuration.xml
chcon u:object_r:vendor_configs_file:s0 /vendor/etc/audio/audio_policy_configuration.xml

mount -o bind $SDIR/support/conf_files/audio_effects-jdsp.xml /vendor/etc/audio_effects.xml
chown root:root /vendor/etc/audio_effects.xml
chmod 0644 /vendor/etc/audio_effects.xml
chcon u:object_r:vendor_configs_file:s0 /vendor/etc/audio_effects.xml

if [ ! -d "$TMPFS" ]; then mkdir "$TMPFS"; fi
mount -t tmpfs tmpfs $TMPFS
cp $SDIR/support/libs/libjamesdsp.so $TMPFS/ 2>/dev/null
cp -av /vendor/lib/soundfx/* $TMPFS/ 2>/dev/null
mount -o bind $TMPFS /vendor/lib/soundfx
chown root:root /vendor/lib/soundfx/*
chmod 0644 /vendor/lib/soundfx/*
chcon u:object_r:vendor_configs_file:s0 /vendor/lib/soundfx/*

mount -o bind /vendor/etc/acdbdata/QRD /vendor/etc/acdbdata/MTP
mount -o bind $SDIR/support/conf_files/mixer_paths_qrd.xml /vendor/etc/mixer_paths_qrd.xml
chown root:root /vendor/etc/mixer_paths_qrd.xml
chmod 0644 /vendor/etc/mixer_paths_qrd.xml
chcon u:object_r:vendor_configs_file:s0 /vendor/etc/mixer_paths_qrd.xml

mount -o bind $SDIR/support/conf_files/default_volume_tables.xml /vendor/etc/default_volume_tables.xml
mount -o bind $SDIR/support/conf_files/audio_policy_volumes.xml /vendor/etc/audio_policy_volumes.xml
chown root:root /vendor/etc/default_volume_tables.xml /vendor/etc/audio_policy_volumes.xml
chmod 0644 /vendor/etc/default_volume_tables.xml /vendor/etc/audio_policy_volumes.xml
chcon u:object_r:vendor_configs_file:s0 /vendor/etc/default_volume_tables.xml /vendor/etc/audio_policy_volumes.xml

killall -q audioserver
killall -q mediaserver
sleep 1
am broadcast -n ${APPLICATION_ID:-james.dsp}/me.timschneeberger.rootlessjamesdsp.retroid.RetroidAudioPolicyReceiver -a ${APPLICATION_ID:-james.dsp}.retroid.AUDIO_POLICY_RESTARTED --ez retroid_after_setup true 2>/dev/null
