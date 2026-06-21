<script setup lang="ts">
import type { SelectOption } from '~/types/code'

defineProps<{
  modelValue: string
  options: SelectOption[]
  name: string
  label?: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <div class="space-y-2">
    <p v-if="label" class="text-sm text-[var(--color-text-muted)]">{{ label }}</p>
    <div class="flex flex-wrap gap-4">
      <label
        v-for="opt in options"
        :key="opt.value"
        class="flex items-center gap-2 text-sm text-[var(--color-text)]"
      >
        <input
          type="radio"
          :name="name"
          :value="opt.value"
          :checked="modelValue === opt.value"
          class="h-4 w-4 accent-[var(--color-primary)]"
          @change="$emit('update:modelValue', opt.value)"
        >
        {{ opt.label }}
      </label>
    </div>
  </div>
</template>
