<script setup lang="ts">
defineOptions({ inheritAttrs: false })

withDefaults(
  defineProps<{
    modelValue: string
    label?: string
    type?: string
    placeholder?: string
    error?: string
    id?: string
  }>(),
  {
    type: 'text'
  }
)

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
      :type="type"
      :placeholder="placeholder"
      :value="modelValue"
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
