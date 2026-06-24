export type ModalMode = 'alert' | 'confirm'
export type ModalVariant = 'default' | 'danger'

export interface ModalOptions {
  title?: string
  message: string
  mode?: ModalMode
  variant?: ModalVariant
  confirmText?: string
  cancelText?: string
  /** 확인 버튼 클릭 시 호출. await modal.confirm(...) 의 resolve(true) 와 별개로 둘 다 실행됨. */
  onConfirm?: () => void
  /** 취소·배경 클릭·ESC 시 호출. */
  onCancel?: () => void
}

/** 모달 스토어가 들고 있는 현재 상태 — isOpen 플래그 + 그 시점의 옵션. */
export interface ModalState extends ModalOptions {
  isOpen: boolean
}
