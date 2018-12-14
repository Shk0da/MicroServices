package com.github.shk0da.micro.cryptography.util;

public final class HSMUtil {

    /**
     * Формирование тела запроса
     *
     * @param data запрос String
     * @return {@link byte[]}
     */
    public static byte[] createRequest(String data) {
        byte[] command = data.getBytes();
        short commandLength = (short) command.length;
        byte[] request = new byte[2 + commandLength];
        request[0] = (byte) (commandLength >> 8 & 0xFF);
        request[1] = (byte) (commandLength & 0xFF);
        System.arraycopy(command, 0, request, 2, (int) commandLength);

        return request;
    }

    /**
     * Формирование тела запроса
     *
     * @param command запрос byte[]
     * @return {@link byte[]}
     */
    public static byte[] createRequest(byte[] command) {
        short commandLength = (short) command.length;
        byte[] request = new byte[2 + commandLength];
        request[0] = (byte) (commandLength >> 8 & 0xFF);
        request[1] = (byte) (commandLength & 0xFF);
        System.arraycopy(command, 0, request, 2, (int) commandLength);

        return request;
    }

    /**
     * Проверка на содержание ошибок в ответе
     *
     * @param response ответ от HSM
     * @return {@link Boolean}
     */
    public static Boolean hasError(byte[] response) {
        return response == null || response.length == 0 || !(response[8] == response[9] && response[9] == 0x30);
    }
}
