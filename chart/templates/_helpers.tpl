{{- define "omar-reachback.imagePullSecret" }}
{{- printf "{\"auths\": {\"%s\": {\"auth\": \"%s\"}}}" .Values.global.imagePullSecret.registry (printf "%s:%s" .Values.global.imagePullSecret.username .Values.global.imagePullSecret.password | b64enc) | b64enc }}
{{- end }}

{{/* Template for env vars */}}
{{- define "omar-reachback.envVars" -}}
  {{- range $key, $value := .Values.envVars }}
  - name: {{ $key | quote }}
    value: {{ $value | quote }}
  {{- end }}
{{- end -}}





{{/* Templates for the volumeMounts section */}}

{{- define "omar-reachback.volumeMounts.configmaps" -}}
{{- range $configmapName, $configmapDict := .Values.configmaps}}
- name: {{ $configmapName | quote }}
  mountPath: {{ $configmapDict.mountPath | quote }}
  {{- if $configmapDict.subPath }}
  subPath: {{ $configmapDict.subPath | quote }}
  {{- end }}
{{- end -}}
{{- end -}}

{{- define "omar-reachback.volumeMounts.pvcs" -}}
{{- range $volumeName := .Values.volumeNames }}
{{- $volumeDict := index $.Values.global.volumes $volumeName }}
- name: {{ $volumeName }}
  mountPath: {{ $volumeDict.mountPath }}
  {{- if $volumeDict.subPath }}
  subPath: {{ $volumeDict.subPath | quote }}
  {{- end }}
{{- end -}}
{{- end -}}

{{- define "omar-reachback.volumeMounts" -}}
{{- include "omar-reachback.volumeMounts.configmaps" . -}}
{{- include "omar-reachback.volumeMounts.pvcs" . -}}
{{- if .Values.global.extraVolumeMounts }}
{{ toYaml .Values.global.extraVolumeMounts }}
{{- end }}
{{- if .Values.extraVolumeMounts }}
{{ toYaml .Values.extraVolumeMounts }}
{{- end }}
{{- end -}}





{{/* Templates for the volumes section */}}

{{- define "omar-reachback.volumes.configmaps" -}}
{{- range $configmapName, $configmapDict := .Values.configmaps}}
- name: {{ $configmapName | quote }}
  configMap:
    name: {{ $configmapName | quote }}
{{- end -}}
{{- end -}}

{{- define "omar-reachback.volumes.pvcs" -}}
{{- range $volumeName := .Values.volumeNames }}
{{- $volumeDict := index $.Values.global.volumes $volumeName }}
- name: {{ $volumeName }}
  persistentVolumeClaim:
    claimName: "{{ $.Values.appName }}-{{ $volumeName }}-pvc"
{{- end -}}
{{- end -}}

{{- define "omar-reachback.volumes" -}}
{{- include "omar-reachback.volumes.configmaps" . -}}
{{- include "omar-reachback.volumes.pvcs" . -}}
{{- if .Values.global.extraVolumes }}
{{ toYaml .Values.global.extraVolumes }}
{{- end }}
{{- if .Values.extraVolumes }}
{{ toYaml .Values.extraVolumes }}
{{- end }}
{{- end -}}
