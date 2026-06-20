<script setup lang="ts">
defineOptions({ inheritAttrs: false })

defineProps<{
  modelValue: string
  options: { value: string; label: string }[]
  label?: string
  placeholder?: string
  error?: string
  id?: string
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
    <select
      :id="id"
      :value="modelValue"
      v-bind="$attrs"
      class="w-full rounded-md border bg-white px-3 py-2 text-sm focus:outline-none"
      :class="error
        ? 'border-red-400 focus:border-red-500'
        : 'border-[var(--color-border)] focus:border-[var(--color-primary)]'"
      @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
    >
      <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
      <option v-for="opt in options" :key="opt.value" :value="opt.value">
        {{ opt.label }}
      </option>
    </select>
    <p v-if="error" class="text-xs text-red-500">{{ error }}</p>
  </div>
</template>
