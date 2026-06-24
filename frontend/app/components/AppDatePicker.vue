<script setup lang="ts">
defineOptions({ inheritAttrs: false })

defineProps<{
  /** ISO 형식 (YYYY-MM-DD) — 백엔드 LocalDate 직렬화 포맷과 동일. */
  modelValue: string
  label?: string
  error?: string
  id?: string
  /** ISO 형식 (YYYY-MM-DD). 선택 가능한 최대 날짜. */
  max?: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <div class="space-y-1">
    <label v-if="label" :for="id" class="text-sm text-[var(--color-text-muted)]">
      {{ label }}
    </label>
    <input
      :id="id"
      type="date"
      :value="modelValue"
      :max="max"
      v-bind="$attrs"
      class="w-full rounded-md border px-3 py-2 text-sm focus:outline-none"
      :class="error
        ? 'border-red-400 focus:border-red-500'
        : 'border-[var(--color-border)] focus:border-[var(--color-primary)]'"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    >
    <p v-if="error" class="text-xs text-red-500">{{ error }}</p>
  </div>
</template>
