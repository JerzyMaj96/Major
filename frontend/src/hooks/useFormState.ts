import { useState } from "react";

export function useFormState<T>(initialState: T) {
  const [values, setValues] = useState<T>(initialState);

  const handleChange = (
    event:
      | React.ChangeEvent<HTMLInputElement>
      | React.ChangeEvent<HTMLTextAreaElement>,
  ) => {
    const { name, value } = event.target;

    setValues((prev) => ({ ...prev, [name]: value }));
  };

  return { values, setValues, handleChange };
}
